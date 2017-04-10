/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.scraelos.esofurnituremp.service;

import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.HierarchicalContainer;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.scraelos.esofurnituremp.model.ESO_SERVER;
import org.scraelos.esofurnituremp.model.FurnitureItem;
import org.scraelos.esofurnituremp.model.ITEM_QUALITY;
import org.scraelos.esofurnituremp.model.Ingredient;
import org.scraelos.esofurnituremp.model.ItemCategory;
import org.scraelos.esofurnituremp.model.ItemScreenshot;
import org.scraelos.esofurnituremp.model.ItemSubCategory;
import org.scraelos.esofurnituremp.model.KnownRecipe;
import org.scraelos.esofurnituremp.model.RECIPE_TYPE;
import org.scraelos.esofurnituremp.model.Recipe;
import org.scraelos.esofurnituremp.model.RecipeIngredient;
import org.scraelos.esofurnituremp.model.SysAccount;
import org.scraelos.esofurnituremp.model.SysAccountRole;
import org.scraelos.esofurnituremp.model.SystemProperty;
import org.scraelos.esofurnituremp.model.lib.DAO;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author scraelos
 */
public class DBService {

    @PersistenceContext
    private EntityManager em;

    @Transactional
    public SysAccount getAccount(String login) throws UsernameNotFoundException {
        SysAccount result = null;
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<SysAccount> criteria = builder.createQuery(SysAccount.class);
        Root<SysAccount> root = criteria.from(SysAccount.class);
        criteria.select(root);
        criteria.where(builder.equal(root.get("username"), login));
        criteria.distinct(true);
        List<SysAccount> resultList = em.createQuery(criteria).getResultList();
        if (resultList != null && !resultList.isEmpty()) {
            result = resultList.get(0);
            result.setKnownRecipesRaw(new HashSet<Recipe>());
            for (KnownRecipe k : result.getKnownRecipes()) {
                result.getKnownRecipesRaw().add(k.getRecipe());
            }
        } else {
            throw new UsernameNotFoundException("username" + login + " not found");
        }
        return result;
    }

    @Transactional
    public SysAccount registerUser(String username, String password, String esoId, ESO_SERVER server) throws Exception {
        SysAccount account = null;
        try {
            getAccount(username);
            throw new Exception("Account already excist");
        } catch (UsernameNotFoundException ex) {
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            String hashedPassword = passwordEncoder.encode(password);
            account = new SysAccount();
            account.setUsername(username);
            account.setEnabled(Boolean.TRUE);
            account.setPassword(hashedPassword);
            account.setEsoId(esoId);
            account.setEsoServer(server);
            Set<SysAccountRole> roles = new HashSet<>();
            roles.add(new SysAccountRole(1L));
            account.setRoles(roles);
            em.persist(account);
        }
        return account;
    }

    @Transactional
    public void createRoles() {
        List<SysAccountRole> roles = new ArrayList<>();
        roles.add(new SysAccountRole(1L, "ROLE_USER", "Login"));
        roles.add(new SysAccountRole(2L, "ROLE_ADMIN", "Admin"));
        roles.add(new SysAccountRole(3L, "ROLE_UPLOAD_SCREENSHOTS", "Upload Screenshots"));
        for (SysAccountRole role : roles) {
            SysAccountRole foundRole = em.find(SysAccountRole.class, role.getId());
            if (foundRole == null) {
                em.persist(role);
            }
        }
    }

    @Transactional
    public void createSystemProperties() {
        List<SystemProperty> propertys = new ArrayList<>();
        propertys.add(new SystemProperty(1L, "smtpServer", null));
        propertys.add(new SystemProperty(2L, "smtpUser", null));
        propertys.add(new SystemProperty(3L, "smtpPassword", null));
        propertys.add(new SystemProperty(4L, "emailFrom", null));
        propertys.add(new SystemProperty(5L, "smtpPort", null));
        propertys.stream().forEach((s) -> {
            SystemProperty find = em.find(SystemProperty.class, s.getId());
            if (find == null) {
                em.persist(s);
            }
        });
    }

    @Transactional
    public void createDefaultAdminUser() {
        try {
            SysAccount account = getAccount("admin@admin.ru");
        } catch (UsernameNotFoundException ex) {
            String password = "admin";
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            String hashedPassword = passwordEncoder.encode(password);
            SysAccount newAdminAccount = new SysAccount();
            newAdminAccount.setUsername("admin@admin.ru");
            newAdminAccount.setEnabled(Boolean.TRUE);
            newAdminAccount.setPassword(hashedPassword);
            Set<SysAccountRole> roles = new HashSet<>();
            roles.add(new SysAccountRole(1L));
            roles.add(new SysAccountRole(2L));
            newAdminAccount.setRoles(roles);
            em.persist(newAdminAccount);
        }
    }

    @Transactional
    public void addItemRecipe(Long id, String textEn, String textDe, String textFr, String textRu) {
        Recipe r = em.find(Recipe.class, id);
        if (r != null) {
            r.setNameEn(textEn);
            r.setNameDe(textDe);
            r.setNameFr(textFr);
            r.setNameRu(textRu);
            em.merge(r);
        } else {
            r = new Recipe();
            r.setId(id);
            r.setNameEn(textEn);
            r.setNameDe(textDe);
            r.setNameFr(textFr);
            r.setNameRu(textRu);
            em.persist(r);
        }
    }

    @Transactional
    public void setItemTranslation(Long id, String textEn, String textDe, String textFr, String textRu) {
        Recipe r = em.find(Recipe.class, id);
        if (r != null) {
            r.setNameEn(textEn);
            r.setNameDe(textDe);
            r.setNameFr(textFr);
            r.setNameRu(textRu);
            em.merge(r);
        } else {
            FurnitureItem f = em.find(FurnitureItem.class, id);
            if (f != null) {
                f.setNameEn(textEn);
                f.setNameDe(textDe);
                f.setNameFr(textFr);
                f.setNameRu(textRu);
                em.merge(f);
            } else {
                Ingredient i = em.find(Ingredient.class, id);
                if (i != null) {
                    i.setNameEn(textEn);
                    i.setNameDe(textDe);
                    i.setNameFr(textFr);
                    i.setNameRu(textRu);
                    em.merge(i);
                }
            }
        }

    }

    @Transactional
    public void addFurnitureRecipe(Long id, String name, RECIPE_TYPE recipeType, ITEM_QUALITY itemQuality, Map<String, Integer> ingredients) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<Recipe> recipeQuery = builder.createQuery(Recipe.class);
        Root<Recipe> recipeRoot = recipeQuery.from(Recipe.class);
        recipeQuery.select(recipeRoot);
        recipeQuery.where(builder.and(
                builder.equal(recipeRoot.get("itemQuality"), itemQuality),
                builder.equal(recipeRoot.get("nameEn"), name)
        )
        );
        recipeQuery.distinct(true);
        Recipe recipe = null;
        List<Recipe> recipeList = em.createQuery(recipeQuery).getResultList();
        if (recipeList == null || recipeList.isEmpty()) {
            CriteriaQuery<Recipe> recipeQuery2 = builder.createQuery(Recipe.class);
            Root<Recipe> recipeRoot2 = recipeQuery2.from(Recipe.class);
            recipeQuery2.select(recipeRoot2);
            recipeQuery2.where(builder.and(
                    builder.isNull(recipeRoot2.get("itemQuality")),
                    builder.equal(recipeRoot2.get("nameEn"), name)
            )
            );
            recipeQuery2.distinct(true);
            recipeList = em.createQuery(recipeQuery2).getResultList();
        }
        if (recipeList == null || recipeList.isEmpty()) {
            FurnitureItem find = em.find(FurnitureItem.class, id);
            if (find != null) {
                if (find.getRecipe() == null) {
                    CriteriaQuery<Recipe> recipeQuery2 = builder.createQuery(Recipe.class);
                    Root<Recipe> recipeRoot2 = recipeQuery2.from(Recipe.class);
                    recipeQuery2.select(recipeRoot2);
                    recipeQuery2.where(builder.and(
                            builder.isNull(recipeRoot2.get("itemQuality")),
                            builder.equal(recipeRoot2.get("nameDe"), find.getNameDe())
                    )
                    );
                    recipeQuery2.distinct(true);
                    recipeList = em.createQuery(recipeQuery2).getResultList();
                } else {
                    recipe=find.getRecipe();
                }

            }

        }
        if (recipe==null&&recipeList != null && !recipeList.isEmpty()) {
            recipe = recipeList.get(0);
        }
        if (recipe!=null) {
            FurnitureItem furnitureItem = em.find(FurnitureItem.class, id);
            recipe.setFurnitureItem(furnitureItem);
            recipe.setItemQuality(itemQuality);
            recipe.setRecipeType(recipeType);
            em.merge(recipe);
            if (furnitureItem != null) {
                furnitureItem.setRecipe(recipe);
                em.merge(furnitureItem);
            } else {
                Logger.getLogger(DBService.class.getName()).info("Can't find " + id);
            }

            for (String key : ingredients.keySet()) {
                Ingredient ingredient = null;
                CriteriaQuery<Ingredient> ingredientQuery = builder.createQuery(Ingredient.class);
                Root<Ingredient> ingredientRoot = ingredientQuery.from(Ingredient.class);
                ingredientQuery.where(builder.equal(ingredientRoot.get("nameEn"), key));
                ingredientQuery.distinct(true);
                List<Ingredient> ingredientList = em.createQuery(ingredientQuery).getResultList();
                if (ingredientList != null && !ingredientList.isEmpty()) {
                    ingredient = ingredientList.get(0);
                } else {
                    ingredient = new Ingredient();
                    ingredient.setNameEn(key);
                    em.persist(ingredient);
                }
                RecipeIngredient recipeIngredient = null;
                CriteriaQuery<RecipeIngredient> recipeIngredientQuery = builder.createQuery(RecipeIngredient.class);
                Root<RecipeIngredient> recipeIngredientRoot = recipeIngredientQuery.from(RecipeIngredient.class);
                recipeIngredientQuery.where(builder.and(
                        builder.equal(recipeIngredientRoot.get("ingredient"), ingredient),
                        builder.equal(recipeIngredientRoot.get("recipe"), recipe)
                )
                );
                recipeIngredientQuery.distinct(true);
                List<RecipeIngredient> recipeIngredientList = em.createQuery(recipeIngredientQuery).getResultList();
                if (recipeIngredientList != null && !recipeIngredientList.isEmpty()) {
                    recipeIngredient = recipeIngredientList.get(0);
                    recipeIngredient.setCount(ingredients.get(key));
                    em.merge(recipeIngredient);
                } else {
                    recipeIngredient = new RecipeIngredient();
                    recipeIngredient.setIngredient(ingredient);
                    recipeIngredient.setCount(ingredients.get(key));
                    recipeIngredient.setRecipe(recipe);
                    em.persist(recipeIngredient);
                }
                /*for(RecipeIngredient ing:recipe.getRecipeIngredients()) {
                 Integer get = ingredients.get(ing.getIngredient().getNameEn());
                 if(get==null) {
                 em.remove(ing);
                 }
                 }*/

            }
        }

    }

    @Transactional
    public HierarchicalContainer getItemCategories() {
        HierarchicalContainer hc = new HierarchicalContainer();
        TypedQuery<ItemCategory> catQ = em.createQuery("select a from ItemCategory a", ItemCategory.class);
        List<ItemCategory> resultList = catQ.getResultList();
        for (ItemCategory cat : resultList) {
            hc.addItem(cat);
            hc.setChildrenAllowed(cat, true);
            for (ItemSubCategory subCat : cat.getItemSubCategorys()) {
                hc.addItem(subCat);
                hc.setParent(subCat, cat);
                hc.setChildrenAllowed(subCat, false);
            }
        }
        return hc;
    }

    @Transactional
    public void addFurnitureItem(Long id, String name, String cat, String subCat, ITEM_QUALITY quality, String itemLink) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<ItemCategory> catQuery = builder.createQuery(ItemCategory.class);
        Root<ItemCategory> catRoot = catQuery.from(ItemCategory.class);
        catQuery.select(catRoot);
        catQuery.where(builder.equal(catRoot.get("nameEn"), cat));
        catQuery.distinct(true);
        List<ItemCategory> catResultList = em.createQuery(catQuery).getResultList();
        ItemCategory itemCategory = null;
        if (catResultList != null && !catResultList.isEmpty()) {
            itemCategory = catResultList.get(0);
        } else {
            itemCategory = new ItemCategory();
            itemCategory.setNameEn(cat);
            em.persist(itemCategory);
        }

        CriteriaQuery<ItemSubCategory> subCatQuery = builder.createQuery(ItemSubCategory.class);
        Root<ItemSubCategory> subCatRoot = subCatQuery.from(ItemSubCategory.class);
        subCatQuery.select(subCatRoot);

        subCatQuery.where(builder.and(
                builder.equal(subCatRoot.get("nameEn"), subCat),
                builder.equal(subCatRoot.get("category"), itemCategory)
        )
        );
        subCatQuery.distinct(true);

        List<ItemSubCategory> subCatResultList = em.createQuery(subCatQuery).getResultList();
        ItemSubCategory itemSubCategory = null;
        if (subCatResultList != null && !subCatResultList.isEmpty()) {
            itemSubCategory = subCatResultList.get(0);
        } else {
            itemSubCategory = new ItemSubCategory();
            itemSubCategory.setNameEn(subCat);
            itemSubCategory.setCategory(itemCategory);
            em.persist(itemSubCategory);
        }

        FurnitureItem item = em.find(FurnitureItem.class, id);
        if (item != null) {
            item.setNameEn(name);
            item.setSubCategory(itemSubCategory);
            item.setItemQuality(quality);
            item.setItemLink(itemLink);
            em.merge(item);
        } else {
            item = new FurnitureItem();
            item.setId(id);
            item.setNameEn(name);
            item.setSubCategory(itemSubCategory);
            item.setItemQuality(quality);
            item.setItemLink(itemLink);
            em.persist(item);

        }
    }

    @Transactional
    public Recipe getRecipe(Long id) {
        return em.find(Recipe.class, id);
    }

    @Transactional
    public boolean isRecipeKnown(Recipe recipe, String characterName, ESO_SERVER server, SysAccount account) {
        boolean result = false;
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<KnownRecipe> q = builder.createQuery(KnownRecipe.class);
        Root<KnownRecipe> root = q.from(KnownRecipe.class);
        q.select(root);

        q.where(builder.and(
                builder.equal(root.get("account"), account),
                builder.equal(root.get("recipe"), recipe),
                builder.equal(root.get("esoServer"), server),
                builder.equal(root.get("characterName"), characterName)
        )
        );
        q.distinct(true);

        List<KnownRecipe> resultList = em.createQuery(q).getResultList();
        if (resultList != null && !resultList.isEmpty()) {
            result = true;
        }
        return result;
    }

    @Transactional
    public List<KnownRecipe> getCrafters(Recipe recipe, ESO_SERVER server) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<KnownRecipe> q = builder.createQuery(KnownRecipe.class);
        Root<KnownRecipe> root = q.from(KnownRecipe.class);
        q.select(root);
        q.where(builder.and(
                builder.equal(root.get("recipe"), recipe),
                builder.equal(root.get("esoServer"), server)
        )
        );
        q.distinct(true);
        List<KnownRecipe> resultList = em.createQuery(q).getResultList();
        List<SysAccount> accounts=new ArrayList<>();
        List<KnownRecipe> newList=new ArrayList<>();
        for(KnownRecipe r:resultList) {
            if(!accounts.contains(r.getAccount())) {
                accounts.add(r.getAccount());
                newList.add(r);
            }
        }
        return newList;
    }

    @Transactional
    public void addKnownRecipes(HierarchicalContainer hc, ESO_SERVER server, SysAccount account) {
        for (Object itemId : hc.getItemIds()) {
            Item item = hc.getItem(itemId);
            String characterName = (String) item.getItemProperty("characterName").getValue();
            Recipe recipe = (Recipe) item.getItemProperty("recipe").getValue();
            KnownRecipe knownRecipe = new KnownRecipe();
            knownRecipe.setAccount(account);
            knownRecipe.setCharacterName(characterName);
            knownRecipe.setEsoServer(server);
            knownRecipe.setRecipe(recipe);
            em.persist(knownRecipe);
        }
    }

    @Transactional
    public void updateUserPassword(SysAccount account, String newPassword) {
        SysAccount a = em.find(SysAccount.class, account.getId());
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String hashedPassword = passwordEncoder.encode(newPassword);
        a.setPassword(hashedPassword);
        em.merge(a);
    }

    @Transactional
    public BeanItemContainer loadBeanItems(BeanItemContainer container) {
        container.removeAllItems();
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery q = builder.createQuery(container.getBeanType());
        Root root = q.from(container.getBeanType());
        q.select(root);
        q.distinct(true);
        container.addAll(em.createQuery(q).getResultList());
        return container;
    }

    @Transactional
    public BeanItemContainer<FurnitureItem> getFurnitureItems(BeanItemContainer<FurnitureItem> container, ItemSubCategory category) {
        container.removeAllItems();
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<FurnitureItem> q = builder.createQuery(FurnitureItem.class);
        Root<FurnitureItem> root = q.from(FurnitureItem.class);
        q.select(root);
        q.where(
                builder.equal(root.get("subCategory"), category)
        );
        q.distinct(true);
        container.addAll(em.createQuery(q).getResultList());
        return container;
    }

    @Transactional
    public void saveEntity(DAO entity) {
        if (entity.getId() != null) {
            em.merge(entity);
        } else {
            em.persist(entity);
        }
    }

    @Transactional
    public void saveUserProfile(SysAccount user) {
        SysAccount account = em.find(SysAccount.class, user.getId());
        account.setEsoServer(user.getEsoServer());
        account.setUseEnItemNames(user.getUseEnItemNames());
        account.setUserLanguage(user.getUserLanguage());
        em.merge(account);
    }

    @Transactional
    public void deleteScreenShot(ItemScreenshot itemScreenshot) {
        ItemScreenshot s = em.find(ItemScreenshot.class, itemScreenshot.getId());
        if (s != null) {
            em.remove(s);
        }

    }

    @Transactional
    public void applyPrices(SysAccount account, ITEM_QUALITY quality, BigDecimal price, BigDecimal priceWithMats, Boolean nullPrice, Boolean nullPriceWithMats) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<KnownRecipe> q1 = cb.createQuery(KnownRecipe.class);
        Root<KnownRecipe> root1 = q1.from(KnownRecipe.class);
        q1.select(root1);
        if (nullPrice) {
            q1.where(cb.and(
                    cb.equal(root1.get("recipe").get("itemQuality"), quality),
                    cb.equal(root1.get("account"), account),
                    cb.isNull(root1.get("craftPrice"))
            ));
        } else {
            q1.where(cb.and(
                    cb.equal(root1.get("recipe").get("itemQuality"), quality),
                    cb.equal(root1.get("account"), account)
            ));
        }
        q1.distinct(true);
        List<KnownRecipe> list1 = em.createQuery(q1).getResultList();
        if (list1 != null && !list1.isEmpty()) {
            for (KnownRecipe r : list1) {
                r.setCraftPrice(price);
                em.merge(r);
            }
        }

        CriteriaQuery<KnownRecipe> q2 = cb.createQuery(KnownRecipe.class);
        Root<KnownRecipe> root2 = q2.from(KnownRecipe.class);
        q2.select(root2);
        if (nullPriceWithMats) {
            q2.where(cb.and(
                    cb.equal(root2.get("recipe").get("itemQuality"), quality),
                    cb.equal(root2.get("account"), account),
                    cb.isNull(root2.get("craftPrice"))
            ));
        } else {
            q2.where(cb.and(
                    cb.equal(root2.get("recipe").get("itemQuality"), quality),
                    cb.equal(root2.get("account"), account)
            ));
        }
        q2.distinct(true);
        List<KnownRecipe> list2 = em.createQuery(q2).getResultList();
        if (list2 != null && !list2.isEmpty()) {
            for (KnownRecipe r : list2) {
                r.setCraftPrice(priceWithMats);
                em.merge(r);
            }
        }
    }
}
