package WxPartion;

// -----( IS Java Code Template v1.2

import com.wm.data.*;
import com.wm.util.Values;
import com.wm.app.b2b.server.Service;
import com.wm.app.b2b.server.ServiceException;
// --- <<IS-START-IMPORTS>> ---
import com.wm.data.IDataCursor;
import com.wm.data.IDataUtil;
import java.nio.charset.StandardCharsets;
import java.util.zip.CRC32;
// --- <<IS-END-IMPORTS>> ---

public final class javaService

{
	// ---( internal utility methods )---

	final static javaService _instance = new javaService();

	static javaService _newInstance() { return new javaService(); }

	static javaService _cast(Object o) { return (javaService)o; }

	// ---( server methods )---




	public static final void sendIdToPartion (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(sendIdToPartion)>> ---
		// @sigtype java 3.5
		// [i] field:0:required id
		// [i] field:0:required totalQueues
		// [i] field:0:required queuePrefix
		// [i] field:0:required oneBasedIndex
		// [o] field:0:required partitionIndex
		// [o] field:0:required targetQueue
		IDataCursor pipelineCursor = pipeline.getCursor();
		
		String id = IDataUtil.getString(pipelineCursor, "id");
		String totalQueuesStr = IDataUtil.getString(pipelineCursor, "totalQueues");
		String queuePrefix = IDataUtil.getString(pipelineCursor, "queuePrefix");
		String oneBasedIndexStr = IDataUtil.getString(pipelineCursor, "oneBasedIndex");
		
		try {
		    // ---------------------------------------------------------
		    // 2. Validate and Parse Inputs
		    // ---------------------------------------------------------
		    if (id == null || id.trim().isEmpty()) {
		        throw new ServiceException("Required input parameter 'id' is missing or empty.");
		    }
		
		    if (totalQueuesStr == null || totalQueuesStr.trim().isEmpty()) {
		        throw new ServiceException("Required input parameter 'totalQueues' is missing or empty.");
		    }
		
		    int totalQueues;
		    try {
		        totalQueues = Integer.parseInt(totalQueuesStr.trim());
		    } catch (NumberFormatException e) {
		        throw new ServiceException("Input 'totalQueues' must be a valid integer. Received: " + totalQueuesStr);
		    }
		
		    if (totalQueues <= 0) {
		        throw new ServiceException("Input 'totalQueues' must be greater than 0. Received: " + totalQueues);
		    }
		
		    // Defaults
		    if (queuePrefix == null || queuePrefix.trim().isEmpty()) {
		        queuePrefix = "q"; // Default prefix
		    } else {
		        queuePrefix = queuePrefix.trim();
		    }
		
		    // Default to true (q1, q2...) unless explicitly set to "false"
		    boolean oneBasedIndex = true;
		    if (oneBasedIndexStr != null && oneBasedIndexStr.trim().equalsIgnoreCase("false")) {
		        oneBasedIndex = false;
		    }
		
		    // ---------------------------------------------------------
		    // 3. Core Stateless Deterministic Partition Logic (CRC32)
		    // ---------------------------------------------------------
		    CRC32 crc = new CRC32();
		    crc.update(id.trim().getBytes(StandardCharsets.UTF_8));
		    long hashValue = crc.getValue(); // Positive 32-bit unsigned integer
		
		    // Compute zero-based partition: 0 to (totalQueues - 1)
		    int zeroBasedPartition = (int) (hashValue % totalQueues);
		
		    // Compute final index based on oneBasedIndex flag
		    int calculatedIndex = oneBasedIndex ? (zeroBasedPartition + 1) : zeroBasedPartition;
		    String computedTargetQueue = queuePrefix + calculatedIndex;
		
		    // ---------------------------------------------------------
		    // 4. Push Results to webMethods Output Pipeline
		    // ---------------------------------------------------------
		    IDataUtil.put(pipelineCursor, "partitionIndex", String.valueOf(calculatedIndex));
		    IDataUtil.put(pipelineCursor, "targetQueue", computedTargetQueue);
		
		} finally {
		    // Always destroy cursor to prevent memory leaks in IS
		    pipelineCursor.destroy();
		}
		// --- <<IS-END>> ---

                
	}
}

