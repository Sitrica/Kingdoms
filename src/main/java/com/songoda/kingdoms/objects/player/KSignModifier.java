package com.songoda.kingdoms.objects.player;

import com.songoda.kingdoms.constants.land.KChestSign;

public interface KSignModifier {
	public KChestSign getModifyingSign();
	public void setModifyingSign(KChestSign sign);
}
