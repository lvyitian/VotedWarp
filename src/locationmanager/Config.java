package locationmanager;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.HashSet;
import java.util.Objects;

public class Config {
  public HashSet<Warp> warpList=new HashSet<Warp>();
  public static class Warp{
	public Warp(HashSet<String> votedPlayers,LinkedHashSet<String> authors,String name,String description, WarpLocation warpLocation) {
		this.votedPlayers=votedPlayers;
		this.authors=authors;
		this.name=name;
		this.description=description;
		this.location=warpLocation;
	}
	  @Override
	  public boolean equals(Object other)
	  {
		  if(!(other instanceof Warp)) return false;
		  Warp o=(Warp)other;
		  return Objects.equals(o.name,this.name);
	  }
	  @Override
	  public int hashCode()
	  {
		  return this.name.hashCode();
	  }
	  public HashSet<String> votedPlayers=new HashSet<>();
	  public LinkedHashSet<String> authors=new LinkedHashSet<>();
	  public String name="";
	  public String description="";
	  public WarpLocation location=new WarpLocation("","",new ArrayList<>());
	  public static class WarpLocation{
		 public WarpLocation(String name, String world, ArrayList<Double> templ) {
			this.name=name;
			this.world=world;
			this.location=templ;
		}
		  public String name="";
		  public String world="";
		  public ArrayList<Double> location=new ArrayList<Double>();
	  }
  }
}
