package com.gsoft.dubbo.common.serialize.support.hh2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.hibernate.Hibernate;
import org.hibernate.collection.internal.AbstractPersistentCollection;
import org.hibernate.collection.internal.PersistentMap;

import com.alibaba.com.caucho.hessian.io.AbstractHessianOutput;
import com.alibaba.com.caucho.hessian.io.CollectionSerializer;
import com.alibaba.com.caucho.hessian.io.HessianProtocolException;
import com.alibaba.com.caucho.hessian.io.MapSerializer;
import com.alibaba.com.caucho.hessian.io.Serializer;
import com.alibaba.com.caucho.hessian.io.SerializerFactory;

/**
 * 序列化
 * 
 * @author LiuPeng
 * 
 */
public class HH2SerializerFactory extends SerializerFactory {

	public static final SerializerFactory SERIALIZER_FACTORY = new HH2SerializerFactory();
	
	private HibernateListSerializer listSerializer = new HibernateListSerializer();
	private HibernateMapSerializer mapSerializer = new HibernateMapSerializer();
	private HibernateBeanSerializer hibernateBeanSerializer = new HibernateBeanSerializer();

	private HH2SerializerFactory() {
	}
	
	@SuppressWarnings("rawtypes")
	public Serializer getSerializer(Class cl) throws HessianProtocolException {
		if (PersistentMap.class.isAssignableFrom(cl)) {
			return mapSerializer;
		} else if (AbstractPersistentCollection.class.isAssignableFrom(cl)) {
			return listSerializer;
		} else if (cl.getSimpleName().contains("_$$_javassist_")) {
			return hibernateBeanSerializer;
		}
		return super.getSerializer(cl);
	}

	private static class HibernateBeanSerializer implements Serializer {
		@Override
		public void writeObject(Object obj, AbstractHessianOutput out) throws IOException {
			boolean init = Hibernate.isInitialized(obj);

			out.writeObject(init ? obj : null);
			out.flush();
			return;
		}
	}

	private static class HibernateListSerializer implements Serializer {
		private CollectionSerializer delegate = new CollectionSerializer();

		@SuppressWarnings({ "unchecked", "rawtypes" })
		public void writeObject(Object obj, AbstractHessianOutput out) throws IOException {
			if (Hibernate.isInitialized(obj)) {
				delegate.writeObject(new ArrayList((Collection) obj), out);
			} else {
				delegate.writeObject(new ArrayList(), out);
			}
		}

	}

	private static class HibernateMapSerializer implements Serializer {
		private MapSerializer delegate = new MapSerializer();

		@SuppressWarnings({ "unchecked", "rawtypes" })
		public void writeObject(Object obj, AbstractHessianOutput out) throws IOException {
			if (Hibernate.isInitialized(obj)) {
				delegate.writeObject(new HashMap((Map) obj), out);
			} else {
				delegate.writeObject(new HashMap(), out);
			}
		}
	}

	@Override
	public ClassLoader getClassLoader() {
		return Thread.currentThread().getContextClassLoader();
	}
}