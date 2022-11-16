package kr.co.klnet.aos.etransdriving.trans.gps.packet;

import com.lbsok.framework.common.FwCommon;
import com.lbsok.framework.network.nio.IProtocolDecoder;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * 응답 패킷 분석
 */
public class ResponsePacket implements IProtocolDecoder
{
	/**
	 * @uml.property  name="intPosition"
	 */
	private int intPosition = 0;
	/**
	 * @uml.property  name="mArbBuffer" multiplicity="(0 -1)" dimension="1"
	 */
	private byte[] mArbBuffer = new byte[FwCommon.MAX_BUFFER_SIZE];

	/**
	 * 암호화된 패킷 디코딩
	 */
	@Override
	public ByteBuffer decode(ByteBuffer bbSocketBuffer) throws IOException
	{
		while (bbSocketBuffer.hasRemaining())
		{
			byte bytCurData = bbSocketBuffer.get();
			try
			{
				mArbBuffer[intPosition] = bytCurData;
			}
			catch (IndexOutOfBoundsException e)
			{
				e.printStackTrace();
				throw new IOException("Packet Too Big. Maximum size allowed: " + FwCommon.MAX_BUFFER_SIZE + " bytes.");
			}

			intPosition++;

			if ((char) bytCurData == ReportPacket.DEF_MOB_PACKET_END_OF_TEXT)
			{
				byte[] bytNewBuffer = new byte[intPosition];
				System.arraycopy(mArbBuffer, 0, bytNewBuffer, 0, intPosition);
				ByteBuffer packetBuffer = ByteBuffer.wrap(bytNewBuffer);
				intPosition = 0;
				return packetBuffer;
			}
		}

		return null;
	}
}