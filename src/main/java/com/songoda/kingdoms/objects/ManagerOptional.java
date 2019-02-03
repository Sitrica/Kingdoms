package com.songoda.kingdoms.objects;

import java.lang.reflect.ParameterizedType;
import java.util.Optional;
import java.util.function.Supplier;

import com.songoda.kingdoms.manager.Manager;
import com.songoda.kingdoms.manager.ManagerHandler;

public class ManagerOptional<T extends Manager> {

	private final Optional<T> optional;
	private final String name;
	
	public ManagerOptional(String name, Optional<T> optional) {
		this.optional = optional;
		this.name = name;
	}
	
	public Object ifNotPresent(Supplier<? extends Object> supplier) {
		return supplier.get();
	}
	
	public T orElseCreate() {
		@SuppressWarnings("unchecked")
		Class<T> clazz = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
		T manager;
		try {
			manager = clazz.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			return null;
		}
		ManagerHandler.registerManager(name, manager);
		return manager;
	}
	
	public Optional<T> getOptional() {
		return optional;
	}
	
	public boolean isPresent() {
		return optional.isPresent();
	}
	
	public T get() {
		return optional.get();
	}

}