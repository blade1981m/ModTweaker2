package modtweaker2.mods.forestry.handlers;

import static modtweaker2.helpers.InputHelper.toFluid;
import static modtweaker2.helpers.InputHelper.toIItemStack;
import static modtweaker2.helpers.InputHelper.toILiquidStack;
import static modtweaker2.helpers.InputHelper.toStack;
import static modtweaker2.helpers.InputHelper.toStacks;
import static modtweaker2.helpers.InputHelper.toShapedObjects;
import static modtweaker2.helpers.StackHelper.matches;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import minetweaker.MineTweakerAPI;
import minetweaker.api.item.IIngredient;
import minetweaker.api.item.IItemStack;
import minetweaker.api.liquid.ILiquidStack;
import modtweaker2.helpers.LogHelper;
import modtweaker2.mods.forestry.ForestryHelper;
import modtweaker2.utils.BaseListAddition;
import modtweaker2.utils.BaseListRemoval;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import stanhebben.zenscript.annotations.Optional;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;
import forestry.core.utils.ShapedRecipeCustom;
import forestry.factory.gadgets.MachineCarpenter;
import forestry.factory.gadgets.MachineCarpenter.Recipe;
import forestry.factory.gadgets.MachineCarpenter.RecipeManager;

@ZenClass("mods.forestry.Carpenter")
public class Carpenter {
    
    public static final String name = "Forestry Carpenter";

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
	/**
	 * Adds a shaped recipe for the Carpenter
	 * 
	 * @param output recipe output
	 * @param fluidInput recipe fluid amount
	 * @param ingredients recipe ingredients
	 * @param packagingTime time per crafting operation
	 * @param box recipes casting item (optional)
	 */
	@ZenMethod
	public static void addRecipe(IIngredient[][] ingredients, int packagingTime, IItemStack output, @Optional ILiquidStack fluidInput, @Optional IItemStack box) {
		MineTweakerAPI.apply(new Add(new Recipe(packagingTime, toFluid(fluidInput), toStack(box), ShapedRecipeCustom.createShapedRecipe(toStack(output), toShapedObjects(ingredients)) )));
	}

	@ZenMethod
	public static void addRecipe(int packagingTime, ILiquidStack liquid, IItemStack[] ingredients, IItemStack ingredient, IItemStack product) {
		ArrayList<ItemStack> stacks = new ArrayList<ItemStack>();
		for (ItemStack stack : toStacks(ingredients)) {
			if (stack != null) {
				stacks.add(stack);
			}
			if (stack == null) {
				stacks.add(new ItemStack(Blocks.air));
			}

		}
		MineTweakerAPI.apply(new Add(new Recipe(packagingTime, toFluid(liquid), toStack(ingredient), new ShapedRecipeCustom(3, 3, toStacks(ingredients), toStack(product)))));
	}

	public ShapedRecipeCustom convertToRecipeCustom() {

		return null;
	}

	private static class Add extends BaseListAddition<Recipe> {

		public Add(Recipe recipe) {
			super(Carpenter.name, MachineCarpenter.RecipeManager.recipes);
			recipes.add(recipe);

			// The Carpenter has a list of valid Fluids, access them via
			// Relfection because of private
			if (recipe.getLiquid() != null)
				ForestryHelper.addCarpenterRecipeFluids(recipe.getLiquid().getFluid());

			if(!RecipeManager.isBox(recipe.getBox())){
				ForestryHelper.addCarpenterRecipeBox(recipe.getBox());
			}
		}
		
		@Override
		protected String getRecipeInfo(Recipe recipe) {
		    return LogHelper.getStackDescription(recipe.getCraftingResult());
		}
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Removes a recipe for the Carpenter
	 * 
	 * @param ingredient item input
	 */
	@ZenMethod
	public static void removeRecipe(IIngredient output, @Optional IIngredient liquid) {
	    List<Recipe> recipes = new LinkedList<Recipe>();
	    
	    for(Recipe recipe : RecipeManager.recipes) {
	        if( recipe != null && recipe.getCraftingResult() != null && matches(output, toIItemStack(recipe.getCraftingResult())) ) {
	        	if (liquid != null) {
	        		if (matches(liquid, toILiquidStack(recipe.getLiquid())))
			            recipes.add(recipe);
	        	} else {
		            recipes.add(recipe);
	        	}
	        }
	    }
	    
	    if(!recipes.isEmpty()) {
	        MineTweakerAPI.apply(new Remove(recipes));
	    } else {
	        LogHelper.logWarning(String.format("No %s Recipe found for %s. Command ignored!", Carpenter.name, output.toString()));
	    }
	    
		
	}

	private static class Remove extends BaseListRemoval<Recipe> {

		public Remove(List<Recipe> recipes) {
			super(Carpenter.name, RecipeManager.recipes, recipes);
		}
		
		@Override
		protected String getRecipeInfo(Recipe recipe) {
		    return LogHelper.getStackDescription(recipe.getCraftingResult());
		}
	}
}
