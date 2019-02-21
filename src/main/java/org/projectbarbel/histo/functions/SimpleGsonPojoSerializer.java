package org.projectbarbel.histo.functions;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.projectbarbel.histo.BarbelHistoContext;
import org.projectbarbel.histo.BarbelMode;
import org.projectbarbel.histo.model.BarbelProxy;
import org.projectbarbel.histo.model.Bitemporal;
import org.projectbarbel.histo.model.BitemporalVersion;

import com.google.gson.Gson;
import com.googlecode.cqengine.persistence.disk.DiskPersistence;
import com.googlecode.cqengine.persistence.offheap.OffHeapPersistence;
import com.googlecode.cqengine.persistence.support.serialization.KryoSerializer;
import com.googlecode.cqengine.persistence.support.serialization.PojoSerializer;

/**
 * Serializer for {@link DiskPersistence} and {@link OffHeapPersistence} that
 * uses Gson and does not require any of the {@link KryoSerializer}
 * functionality.
 * 
 * @author Niklas Schlimm
 *
 */
public class SimpleGsonPojoSerializer implements PojoSerializer<Bitemporal> {

	private Gson gson;
	private static Map<String, Class<?>> typeMap = new HashMap<>();
	private BarbelHistoContext context;

	public SimpleGsonPojoSerializer(BarbelHistoContext context) {
		this.context = context;
		this.gson = context.getGson();
	}

	@Override
	public byte[] serialize(Bitemporal object) {
		if (object instanceof BarbelProxy) { // change persisted type to BitemporalVersion
			Object target = ((BarbelProxy) object).getTarget();
			typeMap.put(target.getClass().getName(), target.getClass());
			object = new BitemporalVersion<>((object).getBitemporalStamp(), ((BarbelProxy) object).getTarget());
		}
		JsonTypeWrapper wrap = new JsonTypeWrapper(object.getClass().getName(), gson.toJson(object));
		return gson.toJson(wrap).getBytes(StandardCharsets.UTF_8);
	}

	@Override
	public Bitemporal deserialize(byte[] bytes) {
		String json = new String(bytes, StandardCharsets.UTF_8);
		JsonTypeWrapper wrap = gson.fromJson(json, JsonTypeWrapper.class);
		Object object = gson.fromJson(wrap.json, typeMap.computeIfAbsent(wrap.type, computeIfAbsent()));
		if (object instanceof BitemporalVersion) {
			BitemporalVersion<?> bv = (BitemporalVersion<?>) object;
			Class<?> objectType = typeMap.computeIfAbsent(bv.getObjectType(), computeIfAbsent());
			Object bvobject = gson.fromJson(gson.toJsonTree(bv.getObject()).toString(), objectType);
			if (context.getMode() == BarbelMode.POJO)
				return context.getMode().snapshotMaiden(context, bvobject, bv.getBitemporalStamp());
			else
				return new BitemporalVersion<>(bv.getBitemporalStamp(), bvobject);
		}
		return (Bitemporal) object;
	}

	private Function<? super String, ? extends Class<?>> computeIfAbsent() {
		return k -> {
			try {
				return Class.forName(k);
			} catch (ClassNotFoundException e) {
				throw new IllegalStateException(
						"failed with ClassNotFoundException on deserializing type from persistence", e);
			}
		};
	}

	public static class JsonTypeWrapper {
		public final String type;
		public final String json;

		public JsonTypeWrapper(String type, String json) {
			super();
			this.type = type;
			this.json = json;
		}
	}
}
