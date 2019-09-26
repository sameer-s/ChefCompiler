package parsermanual;

public abstract class TokenManual {
    public static class Ingredient extends TokenManual {
        public Double amount;
        public boolean isDry;
        public String name;
        public Ingredient(Double amount, boolean isDry, String name) {
            this.amount = amount;
            this.isDry = isDry;
            this.name = name;
        }
    }

    public static class Take extends TokenManual {
        public String ingredientName;
        public Take(String ingredientName) {
            this.ingredientName = ingredientName;
        }
    }

    public static class Put extends TokenManual {
        public String ingredientName;
        public int mixingBowl;

        public Put(String ingredientName) {
            this(ingredientName, -1);
        }

        public Put(String ingredientName, int mixingBowl) {
            this.ingredientName = ingredientName;
            this.mixingBowl = mixingBowl;
        }

        public boolean isUnspecifiedMixingBowl() {
            return mixingBowl == -1;
        }
    }

    public static class Find extends TokenManual {
        public String ingredientName;
        public int mixingBowl;

        public Find(String ingredientName) {
            this(ingredientName, -1);
        }

        public Find(String ingredientName, int mixingBowl) {
            this.ingredientName = ingredientName;
            this.mixingBowl = mixingBowl;
        }

        public boolean isUnspecifiedMixingBowl() {
            return mixingBowl == -1;
        }
    }
}
