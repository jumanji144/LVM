package me.darknet.lua.vm.util;

import java.lang.reflect.Method;
import java.util.function.Consumer;

public class MethodConsumer<T> implements Consumer<T> {

	public Method method;
	public Object instance;

	public MethodConsumer(Object owner, Method method) {
		if(method == null) throw new IllegalArgumentException("method cannot be null");
		if(method.getParameterCount() != 1) throw new IllegalArgumentException("method must have one parameter");
		this.method = method;
		this.instance = owner;
	}

	public void accept(T t) {
		try {
			method.invoke(instance, t);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
