import java.nio.ByteBuffer;
import java.nio.ByteOrder;

enum OpCode {
    STANDARD_QUERY((short)0);

    private final short value;

    OpCode(short value) {
        this.value = value;
    }

    public short getValue() {
        return value;
    }
}

enum ResponseCode {
    NO_ERROR((short)0),
    FORMAT_ERROR((short)1);

    private final short value;

    ResponseCode(short value) {
        this.value = value;
    }

    public short getValue() {
        return value;
    }
}

public class Header implements DNSHeader {
    private short id;
    private short qr;
    private OpCode opCode;
    private short aa;
    private short tc;
    private short rd;
    private short ra;
    private short z;
    private ResponseCode rCode;
    private short qdCount;
    private short anCount;
    private short nsCount;
    private short arCount;

    public Header(short id, short qr, short aa, short tc, short rd, short ra, short z, short qdCount, short anCount, short nsCount, short arCount) {
        this.id = id;
        this.qr = qr;
        this.aa = aa;
        this.tc = tc;
        this.rd = rd;
        this.ra = ra;
        this.z = z;
        this.qdCount = qdCount;
        this.anCount = anCount;
        this.nsCount = nsCount;
        this.arCount = arCount;
    }

    @SuppressWarnings("static-access")
    public byte[] getResponse(byte[] requestData, int requestLength) {
        ByteBuffer requestBuffer = ByteBuffer.wrap(requestData, 0, requestLength);
        int queryId = requestBuffer.getShort() & 0xFFFF;
    
        System.out.println("Query ID: " + queryId);

        short flags = 0;
        
        flags |= this.qr << 15; 
        flags |= this.opCode.STANDARD_QUERY.getValue() << 11; 
        flags |= this.aa << 10; 
        flags |= this.tc << 9;  
        flags |= this.rd << 8;  
        flags |= this.ra << 7;  
        flags |= this.z << 4;  
        flags |= this.rCode.NO_ERROR.getValue();      

        ByteBuffer responseBuffer = ByteBuffer.allocate(12);

        responseBuffer.order(ByteOrder.BIG_ENDIAN);

        responseBuffer.putShort(this.id);
        responseBuffer.putShort(flags);
        responseBuffer.putShort(this.qdCount);
        responseBuffer.putShort(this.anCount);
        responseBuffer.putShort(this.nsCount);
        responseBuffer.putShort(this.arCount);

        byte[] header = responseBuffer.array();

        return header;

    }

}