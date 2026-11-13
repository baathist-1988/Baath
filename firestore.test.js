const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";
const GROUP_ID = "slendytubbies-onlyfans";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function pastTimestamp() {
  return new Date(Date.now() - 2000);
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: cannot read channels, profiles, or messages", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(
    unauthDb.collection("channels").where("groupId", "==", GROUP_ID).get()
  );
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    unauthDb
      .collection("channels")
      .doc("general")
      .collection("messages")
      .where("channelId", "==", "general")
      .get()
  );
});

test("User profile: owner can create and update own profile, cannot access another user's profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const ts = pastTimestamp();

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "TubbyAlice",
      tubbyFlair: "Guardian",
      statusMessage: "Hunting custards since 2013",
      createdAt: ts,
      updatedAt: ts,
    })
  );

  // Update allowed fields
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      displayName: "Alice2013",
      tubbyFlair: "Po",
      statusMessage: "Online on Main Land!",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Shadow field attack fails
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      isAdmin: true,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Bob cannot read or overwrite Alice's private profile
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).update({
      displayName: "Hacked",
      updatedAt: new Date(Date.now() - 1000),
    })
  );
});

test("Fan members directory: signed-in user can create own card and list group members", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const ts = pastTimestamp();

  await assertSucceeds(
    aliceDb.collection("fan_members").doc(ALICE_UID).set({
      userId: ALICE_UID,
      groupId: GROUP_ID,
      displayName: "TubbyAlice",
      tubbyFlair: "Laa-Laa",
      statusMessage: "2013 classic",
      createdAt: ts,
      updatedAt: ts,
    })
  );

  // Aligned query succeeds
  await assertSucceeds(
    bobDb.collection("fan_members").where("groupId", "==", GROUP_ID).get()
  );

  // Unfiltered list fails (rules are not filters)
  await assertFails(bobDb.collection("fan_members").get());

  // Bob cannot spoof Alice's fan_member doc
  await assertFails(
    bobDb.collection("fan_members").doc(ALICE_UID).update({
      displayName: "Spoofed",
      updatedAt: new Date(Date.now() - 1000),
    })
  );
});

test("Channels & Messages: create channel, post message, align list query, enforce ownership on delete", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const ts = pastTimestamp();

  // Create channel
  await assertSucceeds(
    aliceDb.collection("channels").doc("general").set({
      groupId: GROUP_ID,
      slug: "general",
      name: "General",
      description: "2013 main hangout for Slendytubbies fans",
      sortOrder: 0,
      createdBy: ALICE_UID,
      createdAt: ts,
      updatedAt: ts,
    })
  );

  // Post message in existing channel
  await assertSucceeds(
    bobDb
      .collection("channels")
      .doc("general")
      .collection("messages")
      .doc("msg_001")
      .set({
        channelId: "general",
        groupId: GROUP_ID,
        senderId: BOB_UID,
        senderName: "CustardBob",
        senderFlair: "Tinky Winky",
        text: "Anyone playing Slendytubbies v2 tonight?",
        createdAt: ts,
      })
  );

  // Post message to non-existent channel fails (orphan prevention)
  await assertFails(
    bobDb
      .collection("channels")
      .doc("missing_channel")
      .collection("messages")
      .doc("msg_002")
      .set({
        channelId: "missing_channel",
        groupId: GROUP_ID,
        senderId: BOB_UID,
        senderName: "CustardBob",
        senderFlair: "Tinky Winky",
        text: "Orphan message",
        createdAt: ts,
      })
  );

  // Sender spoofing fails
  await assertFails(
    aliceDb
      .collection("channels")
      .doc("general")
      .collection("messages")
      .doc("msg_spoof")
      .set({
        channelId: "general",
        groupId: GROUP_ID,
        senderId: BOB_UID,
        senderName: "CustardBob",
        senderFlair: "Tinky Winky",
        text: "Impersonation attempt",
        createdAt: ts,
      })
  );

  // Aligned list query succeeds
  await assertSucceeds(
    aliceDb
      .collection("channels")
      .doc("general")
      .collection("messages")
      .where("channelId", "==", "general")
      .get()
  );

  // Unfiltered message list fails
  await assertFails(
    aliceDb
      .collection("channels")
      .doc("general")
      .collection("messages")
      .get()
  );

  // Alice cannot delete or update Bob's message
  await assertFails(
    aliceDb
      .collection("channels")
      .doc("general")
      .collection("messages")
      .doc("msg_001")
      .delete()
  );
  await assertFails(
    bobDb
      .collection("channels")
      .doc("general")
      .collection("messages")
      .doc("msg_001")
      .update({ text: "Edited" })
  );

  // Bob can delete his own message
  await assertSucceeds(
    bobDb
      .collection("channels")
      .doc("general")
      .collection("messages")
      .doc("msg_001")
      .delete()
  );
});
