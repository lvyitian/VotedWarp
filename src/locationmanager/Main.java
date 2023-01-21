package locationmanager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryCloseEvent.Reason;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import it.unimi.dsi.fastutil.ints.IntComparators;
import locationmanager.Config.Warp;
import locationmanager.Config.Warp.WarpLocation;

public class Main extends JavaPlugin implements Listener{
	  public static Main instance;
	  {
		  instance=this;
	  }
	  public Config config=null;
	  public String configFileLocation="./plugins/LocationManager/config.json";
	  public ArrayList<String> childCommandList=new ArrayList<String>();
	  public HashMap<String,Inventory> inventoryList=new HashMap<String,Inventory>();
	  public HashMap<Inventory,Long> startList=new HashMap<Inventory,Long>();
	  public ArrayList<String> cancelList=new ArrayList<String>();
	  public void onLoad()
	  {
		  try {
	      childCommandList.add("reload");
		  Bukkit.getLogger().log(Level.INFO, "LocationManager Loaded");
		  }catch(Throwable e) {throw new RuntimeException(e);}
	  }
      public void onEnable()
      { 
    	  try {
          new File(new File(configFileLocation).getParent()).mkdirs();
    	  new File(configFileLocation).createNewFile();
    	  config=parseJson(new String(readFile(new File(configFileLocation)),"GBK"));
    	  if(config==null)
    		  config=new Config();
    	  Bukkit.getPluginManager().registerEvents(this, this);
    	  if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null)
    	  {
    		  new WarpStartsExpansion().register();
    	  }
    	  Bukkit.getLogger().log(Level.INFO, "LocationManager Enabled");
    	  }catch(Throwable e) {throw new RuntimeException(e);}
      }
      public void onDisable()
      {
    	  try {
    	  saveMyConfig();
    	  Bukkit.getLogger().log(Level.INFO, "LocationManager Disabled");
    	  }catch(Throwable e) {throw new RuntimeException(e);}
      }
      @EventHandler(priority=EventPriority.MONITOR,ignoreCancelled=true)
      public void onInventoryClick(InventoryClickEvent e)
      {
    	  try {
    	  if(cancelList.contains(e.getWhoClicked().getUniqueId().toString()))
    	  {
    		  cancelList.remove(e.getWhoClicked().getUniqueId().toString());
    		  e.setCancelled(true);
    	  }
    	  if(inventoryList.containsValue(e.getInventory()))
    	  {
    		  e.setCancelled(true);
    		  if(e.getClickedInventory().equals(e.getInventory()) && e.getCurrentItem().getI18NDisplayName().contains("Warp - "))
    		  {
    			  Player.class.cast(e.getWhoClicked()).playSound(e.getWhoClicked().getLocation(),Sound.ENTITY_PLAYER_LEVELUP,SoundCategory.AMBIENT,100,0);
    			  if(e.isLeftClick())
    			  {
    				  cancelList.add(e.getWhoClicked().getUniqueId().toString());
    				  e.getWhoClicked().closeInventory(Reason.PLUGIN);
    				  if(!e.isShiftClick())
    				  {
        				  Bukkit.dispatchCommand((CommandSender)e.getWhoClicked(), "warp "+e.getCurrentItem().getLore().get(0));      
    				  }else
    				  {
    					  Bukkit.dispatchCommand((CommandSender)e.getWhoClicked(), "votewarp "+e.getCurrentItem().getLore().get(0));
    				  }
    			  }else if(e.isRightClick()){
    				  if(!e.isShiftClick())
    				  {
    					  cancelList.add(e.getWhoClicked().getUniqueId().toString());
        				  e.getWhoClicked().closeInventory(Reason.PLUGIN);
    				  }
    				      Bukkit.dispatchCommand((CommandSender)e.getWhoClicked(), "warpinfo "+e.getCurrentItem().getLore().get(0));  
    			  }
    		  }else if(e.getClickedInventory().equals(e.getInventory()) && e.getCurrentItem().getI18NDisplayName().equals("下一页") && e.isLeftClick() && !e.isShiftClick())
    		  {
    			  Player.class.cast(e.getWhoClicked()).playSound(e.getWhoClicked().getLocation(),Sound.BLOCK_ANVIL_LAND,SoundCategory.AMBIENT,100,0);
    			  startList.replace(e.getInventory(), startList.get(e.getInventory())+1);
    			  Bukkit.dispatchCommand((CommandSender)e.getWhoClicked(), "warplist "+startList.get(e.getInventory()));
    		  }else if(e.getClickedInventory().equals(e.getInventory()) && e.getCurrentItem().getI18NDisplayName().equals("上一页") && e.isLeftClick() && !e.isShiftClick())
    		  {
    			  if(startList.get(e.getInventory())!=1)
    			  {
    				  Player.class.cast(e.getWhoClicked()).playSound(e.getWhoClicked().getLocation(),Sound.BLOCK_ANVIL_LAND,SoundCategory.AMBIENT,100,0);
    				  startList.replace(e.getInventory(), startList.get(e.getInventory())-1);
        			  Bukkit.dispatchCommand((CommandSender)e.getWhoClicked(), "warplist "+startList.get(e.getInventory()));  
    			  }
    		  }
    	  }
    	  }catch(Throwable e2) {}
      }
      @EventHandler(priority=EventPriority.MONITOR,ignoreCancelled=true)
      public void onInventoryClose(InventoryCloseEvent e)
      {
    	  if(inventoryList.containsValue(e.getInventory()))
    	  {
    		  inventoryList.remove(e.getPlayer().getUniqueId().toString());
        	  startList.remove(e.getInventory());
    	  }
      }
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
      {
    	  try {
    	  if(command.getName().equalsIgnoreCase("setwarp"))
    	  {
    		  if(sender instanceof Player)
    		  {
    			  String name;
    			  if(args.length>0)
    				  name=args[0];
    			  else {
    				sender.sendMessage("缺少必须参数name!");
    				return false;
    			  }
    			  LinkedHashSet<String> authors=new LinkedHashSet<>();
    			  authors.add(Player.class.cast(sender).getUniqueId().toString());
    			  if(args.length>1 && !args[1].equals("#"))
    			  {
    				  String a=args[1];
    				  String[] au=a.split(",");
    				  for(String i : au)
    				  {
    					  Optional<OfflinePlayer> p=Lists.newArrayList(Bukkit.getOfflinePlayers()).parallelStream().filter(i2->Objects.equals(i2.getName(),i)).findFirst();
    					  if(p.isEmpty())
    					  {
    						  sender.sendMessage("玩家"+i+"从未进过服务器，忽略！");
    						  continue;
    					  }
    					  authors.add(p.get().getUniqueId().toString());
    				  }
    			  }
    			  StringBuilder desc=new StringBuilder("");
    			  for(int i=2;i<args.length;i++)
    			    desc.append(args[i]+" ");
    			  desc=new StringBuilder(desc.substring(0, desc.length()-1)); 
        		  ArrayList<Double> templ=new ArrayList<Double>();
        		  templ.add(Player.class.cast(sender).getLocation().getX());
        		  templ.add(Player.class.cast(sender).getLocation().getY());
        		  templ.add(Player.class.cast(sender).getLocation().getZ());
        		  templ.add((double) Player.class.cast(sender).getLocation().getYaw());
        		  templ.add((double) Player.class.cast(sender).getLocation().getPitch());
        		  Config.Warp.WarpLocation temph=new Config.Warp.WarpLocation(name,Player.class.cast(sender).getWorld().getName(),templ);
        		  Warp w=config.warpList.parallelStream().filter(i->Objects.equals(i.name,name)).findFirst().orElse(null);
        		  if(w==null)
        		  {
        			  w=new Warp(new HashSet<>(),authors,name,desc.toString(),temph);
        			  config.warpList.add(w);
        			  saveMyConfig();
					  sender.sendMessage("warp已设置!");
        		  }else {
        			  if(sender.hasPermission("locationmanager.admin") || sender.hasPermission("locationmanager.warp.edit") && w.authors.iterator().next().equals(Player.class.cast(sender).getUniqueId().toString()))
        			  {
        				  String[] au=authors.toArray(String[]::new);
        				  au[0]=w.authors.iterator().next();
        				  authors=new LinkedHashSet<>();
        				  authors.addAll(Lists.newArrayList(au));
        				  if(authors.size()>1)
        				    w.authors=authors;
        				  w.description=desc.toString();
        				  w.location=temph;
        				  saveMyConfig();
        			  }else sender.sendMessage("权限不足!");
        		  }
    		  }else sender.sendMessage("此命令只能由玩家执行!");
    	  return true;
    	  }else if(command.getName().equalsIgnoreCase("warp"))
    	  {
    		  if(sender instanceof Player)
    		  {
    			  String name;
    			  if(args.length>0)
    				  name=args[0];
    			  else {
      				sender.sendMessage("缺少必须参数name!");
      				return false;
    			  }
    			  WarpLocation loc=config.warpList.parallelStream().filter(i->Objects.equals(i.name,name)).map(i->i.location).findFirst().orElse(null);
    			  if(loc!=null)
    			  {
    				  Player.class.cast(sender).teleport(new Location(Bukkit.getWorld(loc.world),loc.location.get(0),loc.location.get(1),loc.location.get(2),Float.valueOf(String.valueOf(loc.location.get(3))),Float.valueOf(String.valueOf(loc.location.get(4)))),TeleportCause.PLUGIN);
    				  sender.sendMessage("已传送至"+name+"!");
    			  }else sender.sendMessage("传送点"+name+"不存在!");
    		  }else sender.sendMessage("此命令只能由玩家执行!");
    		  return true;
    	  }else if(command.getName().equalsIgnoreCase("locationmanager"))
    	  {
    		  if(args.length>0)
    		  {
    			  if(args[0].equals("reload"))
    			  {
    				  reloadMyConfig();  
    			  }else {
    				  sender.sendMessage("childCommandList:");
    				  for(int i=0;i<childCommandList.size();i++)
    					  sender.sendMessage(childCommandList.get(i));
    			  }
    			  return true;
    		  }
    	  }else if(command.getName().equalsIgnoreCase("warplist"))
    	  {
    		  long startpage=1;
			    if(args.length>0)
			      startpage=Long.parseLong(args[0]);
			    if(startpage<1)
			    	return true;
  			HashMap<Long,ArrayList<Config.Warp>> pages=new HashMap<Long,ArrayList<Config.Warp>>();
  			long page_temp=1;
  			long tempv=config.warpList.size()/7;
  			if(config.warpList.size()%7!=0)
  				tempv++;
  			for(int i=0;i<tempv;i++)
  			{
  				ArrayList<Config.Warp> temp_s=new ArrayList<Config.Warp>();
  				for(int i2=0;i2<7;i2++)
  				{
  					if(((page_temp-1)*7+i2)>=config.warpList.size())
  						break;
  					temp_s.add(config.warpList.toArray(Warp[]::new)[(int)((page_temp-1)*7+i2)]);
  				}
  				pages.put(page_temp,temp_s);
  				page_temp++;
  			}
  			long max_page=pages.size();
  			if(max_page==0)
  				max_page=1;
  			if(startpage>max_page)
  				startpage=max_page;
    		  if(sender instanceof Player)
    		  {
    			  /*if(args.length>0)
    				Player.class.cast(sender).closeInventory(Reason.PLUGIN);*/
    			  //if(!inventoryList.containsKey(Player.class.cast(sender).getUniqueId().toString())){
    	    			Player.class.cast(sender).closeInventory(Reason.PLUGIN);
    	    			Inventory temp=Bukkit.createInventory((InventoryHolder) sender, 9, "warpList ("+startpage+"/"+max_page+")");
    	    			inventoryList.put(Player.class.cast(sender).getUniqueId().toString(),temp);
        	    		startList.put(temp, startpage);	
    	    			ItemStack pre=new ItemStack(Material.ANVIL);
    	    			ItemMeta pred=pre.getItemMeta();
    	    			pred.setDisplayName("上一页");
    	    			pre.setItemMeta(pred);
    	    			temp.addItem(pre);
    	    			if(pages.get(startpage)!=null)
    	    			{
    	    				for(int i=0;i<pages.get(startpage).size();i++)
        	    			{
        	    				ItemStack temp2=new ItemStack(Material.GRASS,1,(short) 14);
            	    			ItemMeta temp233=temp2.getItemMeta();
            	    			List<String> temp3=new ArrayList<String>();
            	    			temp3.add(String.valueOf(pages.get(startpage).get(i).name));
            	    			temp3.add("作者: "+Arrays.toString(pages.get(startpage).get(i).authors.toArray(String[]::new)));
            	    			temp3.add(pages.get(startpage).get(i).description);
            	    			temp233.setLore(temp3);
            	    		    temp233.setDisplayName("Warp - "+pages.get(startpage).get(i).name);
            	    		    temp2.setItemMeta(temp233);
            	    			temp.addItem(temp2);
        	    			}	
    	    			}
    	    			ItemStack next=new ItemStack(Material.ANVIL);
    	    			ItemMeta nextd=next.getItemMeta();
    	    			nextd.setDisplayName("下一页");
    	    			next.setItemMeta(nextd);
    	    			temp.setItem(8, next);
    	    			Player.class.cast(sender).openInventory(temp);
    	    		  //}  
    		  }else {
    			  sender.sendMessage("warpList ("+startpage+"/"+max_page+")");
    			  if(pages.get(startpage)!=null)
	    		  {
	    			for(int i=0;i<pages.get(startpage).size();i++)
  	    			{
      	    			sender.sendMessage("Warp - "+String.valueOf(pages.get(startpage).get(i).name)+":");
      	    			sender.sendMessage("作者: "+Arrays.toString(pages.get(startpage).get(i).authors.parallelStream().map(UUID::fromString).map(Bukkit::getOfflinePlayer).map(OfflinePlayer::getName).toArray(String[]::new)));
      	    			sender.sendMessage(pages.get(startpage).get(i).description);
  	    			}	
	    		  }
    		  }
    		  return true;
    	  }else if(command.getName().equalsIgnoreCase("warpinfo"))
    	  {
    		String name;
    		if(args.length>0)
    			name=args[0];
    		else {
    			sender.sendMessage("缺少必须参数name!");
        		return false;
    		}
    		Optional<Warp> w=config.warpList.parallelStream().filter(i->Objects.equals(i.name, name)).findFirst();
    		if(w.isEmpty())
    		{
    			sender.sendMessage("传送点"+name+"不存在!");
    			return true;
    		}
    		Warp warp=w.get();
    		sender.sendMessage("\n\n\n-------------\n"+"Authors:"+Arrays.toString(warp.authors.parallelStream().map(UUID::fromString).map(Bukkit::getOfflinePlayer).map(OfflinePlayer::getName).toArray(String[]::new))+"\n"+"Description:"+warp.description+"\n"+"World:"+warp.location.world+"\n"+"X:"+warp.location.location.get(0)+"\n"+"Y:"+warp.location.location.get(1)+"\n"+"Z:"+warp.location.location.get(2)+"\n"+"Yaw:"+warp.location.location.get(3)+"\n"+"Pitch:"+warp.location.location.get(4)+"\n-------------\n");
    		return true;
    	  }else if(command.getName().equalsIgnoreCase("delwarp"))
    	  {
    			  String name;
    			  if(args.length>0)
    				  name=args[0];
    			  else {
    				  sender.sendMessage("缺少必须参数name!");
        			  return false;
    			  }
    			  Optional<Warp> w=config.warpList.parallelStream().filter(i->Objects.equals(i.name,name)).findFirst();
    			  if(w.isEmpty())
    			  {
    				  sender.sendMessage("传送点"+name+"不存在!");
    	    		  return true;
    			  }
    			  Warp warp=w.get();
    			  if(sender.hasPermission("locationmanager.admin") || sender.hasPermission("locationmanager.warp.edit") && warp.authors.iterator().next().equals(Player.class.cast(sender).getUniqueId().toString()))
    	    	  {
    				  config.warpList.remove(warp);
        			  saveMyConfig();
        			  sender.sendMessage("传送点"+name+"已移除!");
    	    	  }else sender.sendMessage("权限不足!");
    		  return true;
    	  }else if(command.getName().equalsIgnoreCase("votewarp"))
		  {
    		  if(sender instanceof Player)
    		  {
    			  String name;
    			  if(args.length>0)
    				  name=args[0];
    			  else {
    				  sender.sendMessage("缺少必须参数name!");
        			  return false;
    			  }
    			  Optional<Warp> w=config.warpList.parallelStream().filter(i->Objects.equals(i.name,name)).findFirst();
    			  if(w.isEmpty())
    			  {
    				  sender.sendMessage("传送点"+name+"不存在!");
    	    		  return true;
    			  }
    			  Player p=(Player)sender;
    			  if(w.get().votedPlayers.add(p.getUniqueId().toString()))
    				  sender.sendMessage("投票成功!");
    			  else sender.sendMessage("您已对此传送点投过票!");
    		  }else sender.sendMessage("此命令只能由玩家执行!");
			  return true;
		  }else if(command.getName().equalsIgnoreCase("warpdesc"))
		  {
			  String name;
			  if(args.length>0)
				  name=args[0];
			  else {
				  sender.sendMessage("缺少必须参数name!");
    			  return false;
			  }
			  Optional<Warp> w=config.warpList.parallelStream().filter(i->Objects.equals(i.name,name)).findFirst();
			  if(w.isEmpty())
			  {
				  sender.sendMessage("传送点"+name+"不存在!");
	    		  return true;
			  }
			  Warp warp=w.get();
			  StringBuilder desc=new StringBuilder("");
			  for(int i=1;i<args.length;i++)
			    desc.append(args[i]+" ");
			  desc=new StringBuilder(desc.substring(0, desc.length()-1));
			  if(sender.hasPermission("locationmanager.admin") || sender.hasPermission("locationmanager.warp.edit") && warp.authors.iterator().next().equals(Player.class.cast(sender).getUniqueId().toString()))
	    	  {
				  warp.description=desc.toString();
    			  saveMyConfig();
    			  sender.sendMessage("已更改传送点"+name+"的描述!");
	    	  }else sender.sendMessage("权限不足!");
			  return true;
		  }else if(command.getName().equalsIgnoreCase("warpauthors"))
		  {
			  String name;
			  if(args.length>0)
				  name=args[0];
			  else {
				  sender.sendMessage("缺少必须参数name!");
    			  return false;
			  }
			  Optional<Warp> w=config.warpList.parallelStream().filter(i->Objects.equals(i.name,name)).findFirst();
			  if(w.isEmpty())
			  {
				  sender.sendMessage("传送点"+name+"不存在!");
	    		  return true;
			  }
			  Warp warp=w.get();
			  LinkedHashSet<String> authors=new LinkedHashSet<>();
			  if(args.length>1)
			  {
				  String a=args[1];
				  String[] au=a.split(",");
				  for(String i : au)
				  {
					  Optional<OfflinePlayer> p=Lists.newArrayList(Bukkit.getOfflinePlayers()).parallelStream().filter(i2->Objects.equals(i2.getName(),i)).findFirst();
					  if(p.isEmpty())
					  {
						  sender.sendMessage("玩家"+i+"从未进过服务器，忽略！");
						  continue;
					  }
					  authors.add(p.get().getUniqueId().toString());
				  }
			  }
			  if(sender.hasPermission("locationmanager.admin") || sender.hasPermission("locationmanager.warp.edit") && warp.authors.iterator().next().equals(Player.class.cast(sender).getUniqueId().toString()))
	    	  {
				  LinkedHashSet<String> newAuthors=new LinkedHashSet<>();
				  newAuthors.add(warp.authors.iterator().next());
				  newAuthors.addAll(authors);
				  warp.authors=newAuthors;
    			  saveMyConfig();
    			  sender.sendMessage("已更改传送点"+name+"的作者列表!");
	    	  }else sender.sendMessage("权限不足!");
			  return true;
		  }else if(command.getName().equalsIgnoreCase("addwarpauthors"))
		  {
			  String name;
			  if(args.length>0)
				  name=args[0];
			  else {
				  sender.sendMessage("缺少必须参数name!");
    			  return false;
			  }
			  Optional<Warp> w=config.warpList.parallelStream().filter(i->Objects.equals(i.name,name)).findFirst();
			  if(w.isEmpty())
			  {
				  sender.sendMessage("传送点"+name+"不存在!");
	    		  return true;
			  }
			  Warp warp=w.get();
			  LinkedHashSet<String> authors=new LinkedHashSet<>();
			  if(args.length>1)
			  {
				  String a=args[1];
				  String[] au=a.split(",");
				  for(String i : au)
				  {
					  Optional<OfflinePlayer> p=Lists.newArrayList(Bukkit.getOfflinePlayers()).parallelStream().filter(i2->Objects.equals(i2.getName(),i)).findFirst();
					  if(p.isEmpty())
					  {
						  sender.sendMessage("玩家"+i+"从未进过服务器，忽略！");
						  continue;
					  }
					  authors.add(p.get().getUniqueId().toString());
				  }
			  }else {
				  sender.sendMessage("缺少必要参数authors!");
				  return false;
			  }
			  if(sender.hasPermission("locationmanager.admin") || sender.hasPermission("locationmanager.warp.edit") && warp.authors.iterator().next().equals(Player.class.cast(sender).getUniqueId().toString()))
	    	  {
				  warp.authors.addAll(authors);
    			  saveMyConfig();
    			  sender.sendMessage("已更改传送点"+name+"的作者列表!");
	    	  }else sender.sendMessage("权限不足!");
			  return true;
		  }else if(command.getName().equalsIgnoreCase("delwarpauthors"))
		  {
			  String name;
			  if(args.length>0)
				  name=args[0];
			  else {
				  sender.sendMessage("缺少必须参数name!");
    			  return false;
			  }
			  Optional<Warp> w=config.warpList.parallelStream().filter(i->Objects.equals(i.name,name)).findFirst();
			  if(w.isEmpty())
			  {
				  sender.sendMessage("传送点"+name+"不存在!");
	    		  return true;
			  }
			  Warp warp=w.get();
			  LinkedHashSet<String> authors=new LinkedHashSet<>();
			  if(args.length>1)
			  {
				  String a=args[1];
				  String[] au=a.split(",");
				  for(String i : au)
				  {
					  Optional<OfflinePlayer> p=Lists.newArrayList(Bukkit.getOfflinePlayers()).parallelStream().filter(i2->Objects.equals(i2.getName(),i)).findFirst();
					  if(p.isEmpty())
					  {
						  sender.sendMessage("玩家"+i+"从未进过服务器，忽略！");
						  continue;
					  }
					  authors.add(p.get().getUniqueId().toString());
				  }
			  }else {
				  sender.sendMessage("缺少必要参数authors!");
				  return false;
			  }
			  if(sender.hasPermission("locationmanager.admin") || sender.hasPermission("locationmanager.warp.edit") && warp.authors.iterator().next().equals(Player.class.cast(sender).getUniqueId().toString()))
	    	  {
				  List<String> t=warp.authors.parallelStream().filter(i->Objects.equals(i, warp.authors.iterator().next())||!authors.contains(i)).collect(Collectors.toList());
				  LinkedHashSet<String> newAuthors=new LinkedHashSet<>();
				  newAuthors.addAll(t);
				  warp.authors=newAuthors;
    			  saveMyConfig();
    			  sender.sendMessage("已更改传送点"+name+"的作者列表!");
	    	  }else sender.sendMessage("权限不足!");
			  return true;
		  }else if(command.getName().equalsIgnoreCase("topwarps"))
		  {
			  long n=10;
			  if(args.length>0)
				  n=Long.parseLong(args[0]);
			  List<Warp> warps=config.warpList.parallelStream().sorted((a,b)->IntComparators.OPPOSITE_COMPARATOR.compare(a.authors.size(), b.authors.size())).collect(Collectors.toList());
			  sender.sendMessage("投票数前"+n+"的传送点列表:");
			  for(int i=0;i<n;i++)
			  {
				  if(i>=warps.size())
					  break;
				  sender.sendMessage((i+1)+":"+warps.get(i).name);
			  }
			  return true;
		  }
    	  return false;
    	  }catch(Throwable e) {throw new RuntimeException(e);}
      }
      public static byte[] readFile(File file) throws Throwable
      {
    	 try(FileInputStream input=new FileInputStream(file))
    	 {
    	 byte[] t_ret=new byte[input.available()];
    	 input.read(t_ret, 0, input.available());
    	 return t_ret;
    	 }
      }
      public static boolean writeFile(File file,byte[] content) throws Throwable
      {
    	  try(FileOutputStream output=new FileOutputStream(file))
    	  {
    	  output.write(content, 0, content.length);
    	  output.flush();
    	  }
    	  return true;
      }
      public static Config parseJson(String json) throws Throwable
      {
    	  Gson parse=new GsonBuilder().setLenient().setPrettyPrinting().enableComplexMapKeySerialization().create();
    	  return parse.fromJson(json, Config.class);
      }
      public static String toJsonString(Config json) throws Throwable
      {
    	  Gson parse=new GsonBuilder().setLenient().setPrettyPrinting().enableComplexMapKeySerialization().create();
    	  return parse.toJson(json);
      }
      public boolean saveMyConfig() throws Throwable
      {
    	  return writeFile(new File(configFileLocation), toJsonString(config).getBytes());
      }
      public void reloadMyConfig() throws Throwable
      {
    	  this.config=parseJson(new String(readFile(new File(configFileLocation)),"GBK"));
      }
}
