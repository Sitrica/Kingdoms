package me.limeglass.kingdoms.objects;

import java.util.Optional;
import java.util.function.Supplier;

import me.limeglass.kingdoms.Kingdoms;
import me.limeglass.kingdoms.manager.Manager;
import me.limeglass.kingdoms.manager.ManagerHandler;

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
	
	@SuppressWarnings("unchecked")
	public <M extends Manager> M orElseCreate(Class<M> expected) {
		if (optional.isPresent())
			return (M) optional.get();
		return managerHandler.createManager(expected);
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