package locationmanager;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public class WarpStartsExpansion extends PlaceholderExpansion {

	@Override
	public @NotNull String getAuthor() {
		return "lvxinlei";
	}

	@Override
	public @NotNull String getIdentifier() {
		return "stars";
	}

	@Override
	public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
		AtomicLong res=new AtomicLong(0);
		String uuid;
		if(Objects.equals(params.trim(),"#") || params.trim().isEmpty())
		{
			if(player==null)
				return null;
			uuid=player.getUniqueId().toString();
		}else {
			Optional<OfflinePlayer> pl=Lists.newArrayList(Bukkit.getOfflinePlayers()).parallelStream().filter(i->Objects.equals(i.getName(),params)).findFirst();
			if(pl.isEmpty())
				return null;
			uuid=pl.get().getUniqueId().toString();
		}
		Main.instance.config.warpList.parallelStream().filter(i->i.authors.contains(uuid)).forEach(i->res.incrementAndGet());
		return String.valueOf(res.longValue());
	}

	@Override
	public boolean canRegister() {
		return Main.instance!=null;
	}

	@Override
	public @Nullable String getRequiredPlugin() {
		return Main.instance.getName();
	}

	@Override
	public @NotNull String getVersion() {
		return "1.0";
	}

}
