namespace scala com.gu.contentapi.client.model.schemaorg
#@namespace typescript _at_guardian.content_api_models.schemaorg

struct SchemaOrg {
    1: optional list<SchemaRecipe> recipe
}

struct SchemaRecipe {
    1: required string _at_context
    2: required string _at_type
    3: optional string name
    4: optional string description
    5: optional string image
    6: optional string datePublished
    7: optional string url
    8: optional list<string> recipeCategory
    9: optional list<string> recipeCuisine
    10: optional list<string> recipeIngredient
    11: optional list<RecipeStep> recipeInstructions
    12: optional list<string> recipeYield
    13: optional string prepTime
    14: optional string cookTime
    15: optional string totalTime
    16: optional AuthorInfo author
    17: optional list<string> suitableForDiet
    18: optional list<string> cookingMethod
}

struct RecipeStep {
    1: required string _at_type
    2: required string text
    3: optional string name
    4: optional string url
    5: optional list<string> image
}

struct AuthorInfo {
    1: required string _at_type
    2: required string name
    3: optional list<string> sameAs
}
