package test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import monero.daemon.MoneroDaemon;
import monero.daemon.model.MoneroBlockHeader;
import monero.daemon.model.MoneroBlockTemplate;
import monero.wallet.MoneroWallet;
import utils.TestUtils;

/**
 * Tests a Monero daemon.
 */
public class TestMoneroDaemon {
  
  // classes to test
  private static MoneroDaemon daemon;
  private static MoneroWallet wallet;
  
  // test configuration
  private static boolean TEST_NON_RELAYS = true;
  private static boolean TEST_RELAYS = true; // creates and relays outgoing txs
  private static boolean TEST_NOTIFICATIONS = true;
  
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    daemon = TestUtils.getDaemonRpc();
    wallet = TestUtils.getWalletRpc();
  }
  
  @Before
  public void before() {
    
  }
  
  // -------------------------------- NON RELAYS ------------------------------
  
  @Test
  public void testIsTrusted() {
    org.junit.Assume.assumeTrue(TEST_NON_RELAYS);
    daemon.getIsTrusted();
  }
  
  @Test
  public void testGetBlockchainHeight() {
    org.junit.Assume.assumeTrue(TEST_NON_RELAYS);
    int height = daemon.getHeight();
    assertTrue("Height must be greater than 0", height > 0);
  }
  
  @Test
  public void testGetBlockIdByHeight() {
    org.junit.Assume.assumeTrue(TEST_NON_RELAYS);
    MoneroBlockHeader lastHeader = daemon.getLastBlockHeader();
    String id = daemon.getBlockId(lastHeader.getHeight());
    assertNotNull(id);
    assertEquals(64, id.length());
  }
  
  @Test
  public void testGetBlockTemplate() {
    org.junit.Assume.assumeTrue(TEST_NON_RELAYS);
    MoneroBlockTemplate template = daemon.getBlockTemplate(TestUtils.TEST_ADDRESS, 2);
    testBlockTemplate(template);
  }
  
  @Test
  public void testGetLastBlockHeader() {
    org.junit.Assume.assumeTrue(TEST_NON_RELAYS);
    MoneroBlockHeader lastHeader = daemon.getLastBlockHeader();
    testBlockHeader(lastHeader, true);
  }
  
  @Test
  public void testGetBlockHeaderById() {
    
    // retrieve by id of last block
    MoneroBlockHeader lastHeader = daemon.getLastBlockHeader();
    String id = daemon.getBlockId(lastHeader.getHeight());
    MoneroBlockHeader header = daemon.getBlockHeaderById(id);
    testBlockHeader(header, true);
    assertEquals(lastHeader, header);
    
    // retrieve by id of previous to last block
    id = daemon.getBlockId(lastHeader.getHeight() - 1);
    header = daemon.getBlockHeaderById(id);
    testBlockHeader(header, true);
    assertEquals(lastHeader.getHeight() - 1, (int) header.getHeight());
  }
  
  @Test
  public void testGetBlockHeaderByHeight() {
    
    // retrieve by height of last block
    MoneroBlockHeader lastHeader = daemon.getLastBlockHeader();
    MoneroBlockHeader header = daemon.getBlockHeaderByHeight(lastHeader.getHeight());
    testBlockHeader(header, true);
   assertEquals(lastHeader, header);
    
    // retrieve by height of previous to last block
    header = daemon.getBlockHeaderByHeight(lastHeader.getHeight() - 1);
    testBlockHeader(header, true);
    assertEquals(lastHeader.getHeight() - 1, (int) header.getHeight());
  }
  
  // TODO: test start with no end, vice versa, inclusivity
  @Test
  public void testGetBlockHeadersByRange() {
    
    // determine start and end height based on number of blocks and how many blocks ago
    int numBlocks = 100;
    int numBlocksAgo = 100;
    int currentHeight = daemon.getHeight();
    int startHeight = currentHeight - numBlocksAgo;
    int endHeight = currentHeight - (numBlocksAgo - numBlocks) - 1;
    
    // fetch headers
    List<MoneroBlockHeader> headers = daemon.getBlockHeadersByRange(startHeight, endHeight);
    
    // test headers
    assertEquals(numBlocks, headers.size());
    for (int i = 0; i < numBlocks; i++) {
      MoneroBlockHeader header = headers.get(i);
      assertEquals(startHeight + i, (int) header.getHeight());
      testBlockHeader(header, true);
    }
  }
  
  // ------------------------------- PRIVATE ---------------------------------
  
  private static void testBlockTemplate(MoneroBlockTemplate template) {
    assertNotNull(template);
    assertNotNull(template.getBlockTemplateBlob());
    assertNotNull(template.getBlockHashingBlob());
    assertNotNull(template.getDifficulty());
    assertNotNull(template.getExpectedReward());
    assertNotNull(template.getHeight());
    assertNotNull(template.getPrevId());
    assertNotNull(template.getReservedOffset());
  }
  
  private static void testBlockHeader(MoneroBlockHeader header, boolean isFull) {
    assertNotNull(header);
    assert(header.getHeight() >= 0);
    assert(header.getMajorVersion() >= 0);
    assert(header.getMinorVersion() >= 0);
    assert(header.getTimestamp() >= 0);
    assertNotNull(header.getPrevId());
    assertNotNull(header.getNonce());
    assertNull(header.getPowHash());  // never seen defined
    if (isFull) {
      assertTrue(header.getSize() > 0);
      assertTrue(header.getDepth() >= 0);
      assertTrue(header.getDifficulty() > 0);
      assertTrue(header.getCumulativeDifficulty() > 0);
      assertEquals(64, header.getId().length());
      assertTrue(header.getNumTxs() >= 0);
      assertNotNull(header.getOrphanStatus());
      assertNotNull(header.getReward());
      assertNotNull(header.getWeight());
    } else {
      assertNull(header.getSize());
      assertNull(header.getDepth());
      assertNull(header.getDifficulty());
      assertNull(header.getCumulativeDifficulty());
      assertNull(header.getId());
      assertNull(header.getNumTxs());
      assertNull(header.getOrphanStatus());
      assertNull(header.getReward());
      assertNull(header.getWeight());
    }
  }
}
