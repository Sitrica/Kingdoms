package com.songoda.kingdoms.objects;

import java.lang.reflect.ParameterizedType;
import java.util.Optional;
import java.util.function.Supplier;

import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.manager.Manager;
import com.songoda.kingdoms.manager.ManagerHandler;

public class ManagerOptional<T extends Manager> {

	private final ManagerHandler managerHandler;
	private final Optional<T> optional;
	
	public ManagerOptional(Optional<T> optional) {
		this.managerHandler = Kingdoms.getInstance().getManagerHandler();
		this.optional = optional;
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
		managerHandler.registerManager(manager);
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