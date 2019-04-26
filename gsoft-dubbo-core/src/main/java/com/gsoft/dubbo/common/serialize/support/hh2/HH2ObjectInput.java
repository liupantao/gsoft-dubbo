package com.gsoft.dubbo.common.serialize.support.hh2;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;

import com.alibaba.com.caucho.hessian.io.Hessian2Input;
import com.alibaba.dubbo.common.serialize.ObjectInput;

/**
 * 
 * @author LiuPeng
 *
 */
public class HH2ObjectInput implements ObjectInput
{
	private final Hessian2Input mH2i;

	public HH2ObjectInput(InputStream is)
	{
		mH2i = new Hessian2Input(is);
		mH2i.setSerializerFactory(HH2SerializerFactory.SERIALIZER_FACTORY);
	}

	public boolean readBool() throws IOException
	{
		return mH2i.readBoolean();
	}

	public byte readByte() throws IOException
	{
		return (byte)mH2i.readInt();
	}

	public short readShort() throws IOException
	{
		return (short)mH2i.readInt();
	}

	public int readInt() throws IOException
	{
		return mH2i.readInt();
	}

	public long readLong() throws IOException
	{
		return mH2i.readLong();
	}

	public float readFloat() throws IOException
	{
		return (float)mH2i.readDouble();
	}

	public double readDouble() throws IOException
	{
		return mH2i.readDouble();
	}

	public byte[] readBytes() throws IOException
	{
		return mH2i.readBytes();
	}

	public String readUTF() throws IOException
	{
		return mH2i.readString();
	}

	public Object readObject() throws IOException
	{
		return mH2i.readObject();
	}

	@SuppressWarnings("unchecked")
	public <T> T readObject(Class<T> cls) throws IOException,
			ClassNotFoundException {
		return (T) mH2i.readObject(cls);
	}

    public <T> T readObject(Class<T> cls, Type type) throws IOException,ClassNotFoundException
    {
        return readObject(cls);
    }

}