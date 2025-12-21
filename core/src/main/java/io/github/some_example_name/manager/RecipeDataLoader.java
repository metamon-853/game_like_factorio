package io.github.some_example_name.manager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import io.github.some_example_name.entity.RecipeData;
import io.github.some_example_name.util.CSVParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * レシピデータをCSVファイルから読み込むクラス。
 * 
 * <p>レシピデータは以下の2つのCSVファイルから読み込まれます：</p>
 * <ul>
 *   <li>recipes.csv: レシピの基本情報</li>
 *   <li>recipe_ingredients.csv: レシピの素材情報</li>
 * </ul>
 * 
 * @author game_like_factorio
 * @version 1.0.0
 */
public class RecipeDataLoader {
    private static RecipeDataLoader instance;
    private Map<Integer, RecipeData> recipes;
    private List<RecipeData> recipeList;
    
    /**
     * RecipeDataLoaderのシングルトンインスタンスを取得します。
     * @return RecipeDataLoaderのインスタンス
     */
    public static RecipeDataLoader getInstance() {
        if (instance == null) {
            instance = new RecipeDataLoader();
        }
        return instance;
    }
    
    /**
     * RecipeDataLoaderを初期化します。
     */
    private RecipeDataLoader() {
        this.recipes = new HashMap<>();
        this.recipeList = new ArrayList<>();
    }
    
    /**
     * レシピデータを読み込みます。
     * 
     * @return 読み込みに成功した場合true
     */
    public boolean loadRecipes() {
        try {
            // recipes.csvを読み込む
            FileHandle recipesFile = Gdx.files.internal("recipes.csv");
            if (!recipesFile.exists()) {
                Gdx.app.log("RecipeDataLoader", "recipes.csv not found, skipping recipe loading");
                return false;
            }
            
            String recipesContent = recipesFile.readString("UTF-8");
            String normalizedContent = CSVParser.normalizeLineEndings(recipesContent);
            String[] lines = normalizedContent.split("\n");
            
            if (lines.length < 2) {
                Gdx.app.error("RecipeDataLoader", "recipes.csv is empty or invalid");
                return false;
            }
            
            // ヘッダー行をスキップ
            for (int i = 1; i < lines.length; i++) {
                String line = lines[i].trim();
                if (line.isEmpty()) {
                    continue;
                }
                
                List<String> parts = CSVParser.parseCSVLine(line);
                if (parts.size() < 6) {
                    Gdx.app.log("RecipeDataLoader", "Skipping invalid recipe line: " + line);
                    continue;
                }
                
                try {
                    RecipeData recipe = new RecipeData();
                    recipe.id = Integer.parseInt(parts.get(0));
                    recipe.resultItemId = Integer.parseInt(parts.get(1));
                    recipe.resultAmount = parts.size() > 2 && !parts.get(2).isEmpty() ? 
                        Integer.parseInt(parts.get(2)) : 1;
                    recipe.name = parts.size() > 3 ? parts.get(3) : "";
                    recipe.description = parts.size() > 4 ? parts.get(4) : "";
                    recipe.requiredCivilizationLevel = parts.size() > 5 && !parts.get(5).isEmpty() ? 
                        Integer.parseInt(parts.get(5)) : 1;
                    recipe.category = parts.size() > 6 ? parts.get(6) : "その他";
                    
                    recipes.put(recipe.id, recipe);
                    recipeList.add(recipe);
                } catch (NumberFormatException e) {
                    Gdx.app.error("RecipeDataLoader", "Error parsing recipe line: " + line, e);
                }
            }
            
            // recipe_ingredients.csvを読み込む
            FileHandle ingredientsFile = Gdx.files.internal("recipe_ingredients.csv");
            if (ingredientsFile.exists()) {
                String ingredientsContent = ingredientsFile.readString("UTF-8");
                String normalizedIngredients = CSVParser.normalizeLineEndings(ingredientsContent);
                String[] ingredientLines = normalizedIngredients.split("\n");
                
                if (ingredientLines.length >= 2) {
                    // ヘッダー行をスキップ
                    for (int i = 1; i < ingredientLines.length; i++) {
                        String line = ingredientLines[i].trim();
                        if (line.isEmpty()) {
                            continue;
                        }
                        
                        List<String> parts = CSVParser.parseCSVLine(line);
                        if (parts.size() < 3) {
                            Gdx.app.log("RecipeDataLoader", "Skipping invalid ingredient line: " + line);
                            continue;
                        }
                        
                        try {
                            int recipeId = Integer.parseInt(parts.get(0));
                            int itemId = Integer.parseInt(parts.get(1));
                            int amount = Integer.parseInt(parts.get(2));
                            
                            RecipeData recipe = recipes.get(recipeId);
                            if (recipe != null) {
                                recipe.addIngredient(itemId, amount);
                            } else {
                                Gdx.app.log("RecipeDataLoader", "Recipe not found for ingredient: recipeId=" + recipeId);
                            }
                        } catch (NumberFormatException e) {
                            Gdx.app.error("RecipeDataLoader", "Error parsing ingredient line: " + line, e);
                        }
                    }
                }
            } else {
                Gdx.app.log("RecipeDataLoader", "recipe_ingredients.csv not found, recipes will have no ingredients");
            }
            
            Gdx.app.log("RecipeDataLoader", "Loaded " + recipes.size() + " recipes");
            return true;
        } catch (Exception e) {
            Gdx.app.error("RecipeDataLoader", "Error loading recipes: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 指定されたIDのレシピを取得します。
     * @param recipeId レシピID
     * @return レシピデータ（存在しない場合はnull）
     */
    public RecipeData getRecipe(int recipeId) {
        return recipes.get(recipeId);
    }
    
    /**
     * すべてのレシピを取得します。
     * @return レシピのリスト
     */
    public List<RecipeData> getAllRecipes() {
        return new ArrayList<>(recipeList);
    }
    
    /**
     * 指定されたアイテムIDのレシピを取得します。
     * @param itemId アイテムID
     * @return レシピデータのリスト
     */
    public List<RecipeData> getRecipesForItem(int itemId) {
        List<RecipeData> result = new ArrayList<>();
        for (RecipeData recipe : recipeList) {
            if (recipe.resultItemId == itemId) {
                result.add(recipe);
            }
        }
        return result;
    }
    
    /**
     * アンロックされているレシピを取得します。
     * @return アンロックされているレシピのリスト
     */
    public List<RecipeData> getUnlockedRecipes() {
        List<RecipeData> result = new ArrayList<>();
        for (RecipeData recipe : recipeList) {
            if (recipe.isUnlocked()) {
                result.add(recipe);
            }
        }
        return result;
    }
    
    /**
     * 指定された文明レベルでアンロック可能なレシピをアンロックします。
     * @param civilizationLevel 文明レベル
     */
    public void unlockRecipesForLevel(int civilizationLevel) {
        for (RecipeData recipe : recipeList) {
            if (recipe.requiredCivilizationLevel <= civilizationLevel) {
                recipe.setUnlocked(true);
            }
        }
    }
    
    /**
     * すべてのレシピをクリアします。
     */
    public void clear() {
        recipes.clear();
        recipeList.clear();
    }
}
