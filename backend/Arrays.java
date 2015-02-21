package backend;

import java.util.ArrayList;

//Values used for display throughout the GUI, also for Key-Value DB
public class Arrays {
	
	//fish is unused
	public static final int FISH = 0, AMPHIBIAN = 1, BIRD = 2, INVERTEBRATE = 3,
							MAMMAL = 4, REPTILE = 5;
	
	//Used to populate selection boxes for search/submission
	public static String[] types() {
		String[] types = { " ", "Amphibian", "Bird", "Invertebrate", "Mammal", "Reptile", "testdb"};
		return types;
	}
	
	//Used to only fetch/display relevant types (i.e. not test data)
	public static String[] onlyValidDBTypes() {
		String[] types = {"Amphibian", "Bird", "Invertebrate", "Mammal", "Reptile"};
		return types;
	}
	
	/* Used to populate subtype selection boxes on selection of type
	 * Values as provided by Tadzio
	 */
	public static String[] getSubtypes(int type) {
		String[] subtypes;
		switch(type) {
		case AMPHIBIAN:
			subtypes = new String[] {"", "Caecilians", "Frogs & Toads", "Salamanders"};
			break;
		case BIRD:
			subtypes = new String[] {"", "Anhingas", "Antbirds", "Bananaquit", "Barbets", "Becards",
					"Blackbirds", "Boobies", "Buntings", "Chachalacas", "Cormorants", "Cotingas", 
					"Crakes", "Cuckoos", "Curassows", "Dippers", "Doves", "Ducks", "Eagles", 
					"Euphonias", "Falcons", "Finches", "Flycatchers", "Frigatebirds", "Gnatcatchers",
					"Gnatwrens", "Goldfinches", "Grebes", "Grosbeaks", "Guans", "Gulls", "Hawks", 
					"Herons", "Hummingbirds", "Ibises", "Jaçanas", "Jacamars", "Jays", "Kingfishers",
					"Limpkin", "Manakins", "Mockingbirds", "Motmots", "Munias", "Nightjars", 
					"Oilbirds", "Orioles", "Ovenbirds", "Owls", "Oystercatchers", "Parrots",
					"Pelicans",	"Pigeons", "Plovers", "Potoos", "Puffbirds", "Quails", "Rails",
					"Saltators", "Sandpipers", "Seedeaters", "Sharpbill", "Skimmers", "Sparrows",
					"Spoonbills", "Stilts", "Storks", "Sunbittern", "Sungrebes", "Swallows", 
					"Swifts", "Tanagers", "Tapaculos", "Terns", "Thick-knees", "Thrushes", "Tinamous", 
					"Toucans", "Trogons", "Vireos", "Vultures", "Waxwings", "Wood-Warbles", 
					"Woodcreepers", "Woodpeckers", "Wrens"};
			break;
		case INVERTEBRATE:
			subtypes = new String[] {"", "Ants", "Bees", "Beetles", "Butterflies", "Crickets", 
					"External Parasites", "Flies", "Grasshoppers", "Harvestmen", "Jumping Spiders", 
					"Katydids", "Leaf Insects", "Mosquitoes", "Moths", "Plant-Hoppers", 
					"Praying Mantises", "Scorpions", "Stick Insects", "Tailless Whip Scorpions", 
					"Tarantulas", "Wasps", "Web Spinners", "Wolf Spiders"};
			break;
		case MAMMAL:
			subtypes = new String[]{"", "Anteaters", "Armadillos", "Bats", "Cat Family", "Deer", 
					"Dog Family", "Manatees", "Monkeys", "Opossums", "Peccaries", "Rabbits", 
					"Raccoon Family", "Rodents", "Shrews", "Sloths", "Tapirs", "Weasel Family", 
					"Whales & Dolphins"};
			break;
		case REPTILE:
			subtypes = new String[] {"", "Anoles", "Basilisks", "Crocodilians", "Geckos", "Iguanas",
					"Lizards", "Sea Turtles", "Skinks", "Snakes", "Turtles"};
			break;
		case FISH:
		default:
			subtypes = new String[] {""};
			break;
		}
		return subtypes;
	}
	
	//Used to populate colour selection boxes
	public static String[] getColours() {
		String[] colours = new String[] {"", "Black", "Blue", "Brown", "Green", "Grey", "Orange", 
				"Pink", "Purple", "Red", "White", "Yellow"};
		return colours;
	}
	
	//Labels for input fields in a specific order for layout
	public static ArrayList<String> fieldLabels() {
		String[] labelValues = new String[] {"Animal ID", "English Name", "Subtype", "Spanish Name", 
				"Pattern", "Latin Name", "Abundancy", "Other English", "Wingspan", "Other Spanish", 
				"Reproduction", "Size", "Longevity", "Weight", "Diet", "Range", "Sociability", 
				"Elevation", "Colours", "Conservation Status", "Did You Know", "Comment"};
		ArrayList<String> labels = new ArrayList<>();
		for (String s : labelValues) {
			labels.add(s);
		}
		return labels;
	}
	
	//ArrayList needed for searching Amazon's DynamoDB 
	public static ArrayList<String> columnsAsArrayList() {
		String[] columns = columnsAsArray();
		ArrayList<String> ret = new ArrayList<>();
		for (String s : columns) {
			ret.add(s);
		}
		return ret;
	}
	
	//Used for GUI layout
	public static String[] columnsAsArray() {
		String[] columns = {"Animal ID", "Subtype", "English Name", "Latin Name", "Spanish Name", 
				"Other English", "Other Spanish", "Pattern", "Colour 1", "Colour 2", "Colour 3", 
				"Abundancy", "Sociability", "Reproduction", "Longevity", "Conservation Status", 
				"Size", "Weight", "Diet", "Wingspan", "Range", "Elevation", "Diurnal", "Nocturnal", 
				"Arboreal", "Terrestrial", "Aquatic", "Complete", "Did You Know", "Comment"};
		return columns;
	}
	
	public static String[] validPatterns() {
		return new String[] {"", "Mottled", "Spotted", "Solid", "Striped", "Mottled and Spotted", 
				"Mottled and Solid", "Mottled and Striped", "Spotted and Solid", "Spotted and Striped", 
				"Solid and Striped"};
	}
	
	public static String[] prevalence() {
		return new String[] {"", "Common", "Uncommon", "Rare"};
	}
	
	public static String[] conservationStatus() {
		return new String[] {"", "Conservation Dependent", "Critically Endangered", "Data Deficient", 
				"Endangered", "Extinct", "Extinct in the Wild", "Least Concern", "Near Threatened", 
				"Vulnerable"};
	}

}