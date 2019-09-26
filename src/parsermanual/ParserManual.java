package parsermanual;

import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParserManual {
    private String[] input;
    private int i;

    private static final String WHITESPACE = "\\s+";
    private static final String ALL_REMAINING = "\\A";

    public ParseTree parse(String[] input) {
        this.input = input;
        this.i = 0;

        // TODO: better error handling
        try {
            return parseHelper();
        } catch (ParserException e) {
            ParserException modified = new ParserException("Error at line: %s%n%s", input[i], e.getMessage());
            modified.setStackTrace(e.getStackTrace());
            throw modified;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ParserException("Found unexpected end-of-file");
        }
    }

    private ParseTree parseHelper() {
        // Recipe Title
        if (!input[i].trim().endsWith(".")) {
            throw new ParserException("Recipe name must end with full stop");
        }
        i++;

        if (matchBlankLines() == 0) {
            throw new ParserException("Blank line not found after recipe title");
        }

        // Comments [optional]
        boolean commentsStarted = false; // Tracks whether or not the comments section even exists
        boolean commentsEnded = false; // If it does exist, tracks when we've reached the end (a blank line)
        while (!input[i].trim().equals("Ingredients.")) {
            commentsStarted = true;
            if (input[i].isBlank()) {
                commentsEnded = true;
            } else if (commentsEnded) {
                throw new ParserException("Comments must be a single free-form paragraph");
            }
            i++;
        }

        if (commentsStarted && !commentsEnded) {
            throw new ParserException("Blank line not found after comments");
        }

        // Ingredients
        i++; // We don't care about the actual "Ingredients." line, it has already been matched
        while (!input[i].isBlank()) {
            // In theory, a blank line should signify the end of the ingredients section
            parseIngredient(input[i].trim()); // TODO do something with this token
            i++;
        }

        matchBlankLines();

        // Cooking Time [optional]
        if (input[i].trim().startsWith("C")) {
            Scanner sc = new Scanner(input[i].trim());
            sc.useDelimiter(WHITESPACE);

            sc.next(); // Match "Cooking time:"

            String time = sc.next();
            try {
                Double.parseDouble(time); // Parse the number time
            } catch (NumberFormatException nfe) {
                throw new ParserException("Cooking time must be a number, was: %s", time);
            }

            String unit = sc.next();
            if (!unit.equals("hour.") && !unit.equals("hours.") && !unit.equals("minute.") && !unit.equals("minutes.")) {
                throw new ParserException("Cooking time unit must be hour[s] or minute[s] followed by a full stop, was: %s", unit);
            }

            String rest = sc.nextLine();
            if (!rest.isBlank()) {
                throw new ParserException("Extraneous characters %s after full stop in cooking time", rest);
            }

            sc.close();
            i++;
            if (matchBlankLines() == 0) {
                throw new ParserException("Blank line not found after cooking time");
            }
        }

        // Oven Temperature [optional]
        if (input[i].trim().startsWith("P")) {
            String[] split = input[i].trim().split(WHITESPACE);
            if (split.length == 6) {
                String[] format = {"Pre-heat", "oven", "to", ".+", "degrees", "Celsius\\."};
                if (input[i].trim().matches(String.join(WHITESPACE, format))) {
                    try {
                        Double.parseDouble(split[3]);
                    } catch (NumberFormatException nfe) {
                        throw new ParserException("Oven pre-heat temperature must be number");
                    }
                } else {
                    throw new ParserException("Oven temperature statement did not match format");
                }
            } else if (split.length == 9) {
                String[] format = {"Pre-heat", "oven", "to", ".+", "degrees", "Celsius", "\\(gas", "mark", "*\\)\\."};
                if (input[i].trim().matches(String.join(WHITESPACE, format))) {
                    try {
                        Double.parseDouble(split[3]);
                    } catch (NumberFormatException nfe) {
                        throw new ParserException("Oven pre-heat temperature must be number");
                    }
                    try {
                        Double.parseDouble(split[8].substring(0, split[8].length() - 2));
                    } catch (NumberFormatException nfe) {
                        throw new ParserException("Oven gas mark must be number");
                    }
                } else {
                    throw new ParserException("Oven temperature statement did not match format");
                }
            } else {
                throw new ParserException("Oven temperature segment must contain 6 or 9 words, contained %d", split.length);
            }

            if (matchBlankLines() == 0) {
                throw new ParserException("Blank line not found after oven temperature");
            }
        }

        // Method
        if (input[i].trim().equals("Method.")) {
            while (true) {
                int linesSkipped = matchBlankLines();
                if (input[i].trim().startsWith("Serves")) {
                    if (linesSkipped == 0) {
                        throw new ParserException("Blank line not found after method statement");
                    } else {
                        break;
                    }
                }
                for (String statement : input[i].trim().split("\\.")) {
                    parseMethodStatement(statement); // TODO do something with this token
                }
                i++;
            }
        } else {
            throw new ParserException("Method block must begin with \"Method.\" statement");
        }

        // Serves
        String servesFormat = "Serves" + WHITESPACE + ".+\\.";
        if (input[i].trim().matches(servesFormat)) {
            String servesStr = input[i].trim().substring(7, input[i].trim().length() - 1);
            try {
                double servesNum = Double.parseDouble(servesStr);
                // TODO use servesNum value
            } catch (NumberFormatException nfe) {
                throw new ParserException("Number of diners in serve statement must be a number, was: %s", servesStr);
            }
        } else {
            throw new ParserException("Invalid format of serves statement");
        }

        return null;
    }

    private TokenManual.Ingredient parseIngredient(String ingredientLine) {
        Scanner sc = new Scanner(ingredientLine.trim());
        sc.useDelimiter(WHITESPACE);

        Double amount;
        boolean isDry;
        String name = "";

        String current = sc.next();
        try {
            amount = Double.parseDouble(current);
            current = sc.next();
        } catch (NumberFormatException nfe) {
            amount = null;
        }
        if (current.equals("heaped") || current.equals("level")) {
            String measure = sc.next();
            isDry = isDry(measure, current);
        } else if (isMeasure(current)){
            isDry = isDry(current, null);
        } else {
            isDry = true;
            name = current;
        }

        sc.useDelimiter(ALL_REMAINING);
        if (sc.hasNext()) {
            name += sc.next();
        } else if (name.equals("")) {
            throw new ParserException("No name found for ingredient");
        }
        sc.close();
        return new TokenManual.Ingredient(amount, isDry, name);
    }

    private TokenManual parseMethodStatement(String methodLine) {
        Scanner sc = new Scanner(methodLine.trim());
        sc.useDelimiter(WHITESPACE);
        String keyword = sc.next();
        sc.useDelimiter(ALL_REMAINING);
        String remaining = sc.hasNext() ? sc.next() : "";
        sc.close();

        switch (keyword) {
            case "Take": {
                String[] format = { ".+", "from", "refrigerator" };
                Pattern p = Pattern.compile(String.join(WHITESPACE, format));
                Matcher m = p.matcher(remaining);
                if (!m.find()) {
                    throw new ParserException("Take expression \"%s\" does not match proper format", methodLine.trim());
                }
                return new TokenManual.Take(m.group());
            }
            case "Put": {
                String[] format1 = { ".+", "into", "the", "mixing", "bowl" };
                String[] format2 = { ".+", "into", "the", "\\d+(?=st|nd|rd|th)", "mixing", "bowl" };
                Pattern p1 = Pattern.compile(String.join(WHITESPACE, format1));
                Pattern p2 = Pattern.compile(String.join(WHITESPACE, format2));
                Matcher m1 = p1.matcher(remaining);
                Matcher m2 = p2.matcher(remaining);
                if (m1.find()) {
                    return new TokenManual.Put(m1.group());
                } else if (m2.find()) {
                    return new TokenManual.Put(m2.group(), Integer.parseInt(m2.group()));
                } else {
                    throw new ParserException("Put expression \"%s\" does not match proper format", methodLine.trim());
                }
            }
            case "Fold": {
                String[] format1 = { ".+", "into", "the", "mixing", "bowl" };
                String[] format2 = { ".+", "into", "the", "\\d+(?=st|nd|rd|th)", "mixing", "bowl" };
                Pattern p1 = Pattern.compile(String.join(WHITESPACE, format1));
                Pattern p2 = Pattern.compile(String.join(WHITESPACE, format2));
                Matcher m1 = p1.matcher(remaining);
                Matcher m2 = p2.matcher(remaining);
                if (m1.find()) {
                    return new TokenManual.Find(m1.group());
                } else if (m2.find()) {
                    return new TokenManual.Find(m2.group(), Integer.parseInt(m2.group()));
                } else {
                    throw new ParserException("Find expression \"%s\" does not match proper format", methodLine.trim());
                }
            }
            case "Add": {
                // Add or add dry
                String[] format1 = { ".+", "to", "mixing", "bowl" };
                String[] format2 = { ".+", "to", "\\d+(?=st|nd|rd|th)", "mixing", "bowl" };
                String dryIngredients = "dry" + WHITESPACE + "ingredients";

                Pattern p1 = Pattern.compile(String.join(WHITESPACE, format1));
                Pattern p2 = Pattern.compile(String.join(WHITESPACE, format2));
                Matcher m1 = p1.matcher(remaining);
                Matcher m2 = p2.matcher(remaining);
                if (m1.find()) {
                    return new TokenManual.Find(m1.group());
                } else if (m2.find()) {
                    return new TokenManual.Find(m2.group(), Integer.parseInt(m2.group()));
                } else {
                    throw new ParserException("Find expression \"%s\" does not match proper format", methodLine.trim());
                }

            }
            case "Remove":
            case "Combine":
            case "Divide":
            case "Liquify":
//                System.err.println("[Warn] Liquify is deprecated, use Liquefy instead");
            case "Liquefy":
            case "Stir":
                // Stir mixing bowl or stir ingredient into mixing bowl
            case "Mix":
            case "Clean":
            case "Pour":
            case "Set":
            case "Serve":
            case "Refrigerate":
            default:
                // Verb the ingredient [until verbed]
                return null;
        }
    }

    private static boolean isMeasure(String measure) {
        switch(measure) {
            case "g":
            case "kg":
            case "pinch":
            case "pinches":
            case "ml":
            case "l":
            case "dash":
            case "dashes":
            case "cup":
            case "cups":
            case "teaspoon":
            case "teaspoons":
            case "tablespoon":
            case "tablespoons":
                return true;
        }
        return false;
    }


    private static boolean isDry(String measure, String measureType) {
        switch (measure) {
            case "g":
            case "kg":
            case "pinch":
            case "pinches":
                return true;
            case "ml":
            case "l":
            case "dash":
            case "dashes":
                if (measureType != null) {
                    // Both explicit measure types are dry-only
                    throw new ParserException("Measure type %s is not compatible with liquid measure %s", measureType, measure);
                }
                return false;
            case "cup":
            case "cups":
            case "teaspoon":
            case "teaspoons":
            case "tablespoon":
            case "tablespoons":
                if (measureType == null) {
                    return true;
                } else {
                    if (!measureType.equals("heaped") && !measureType.equals("level")) {
                        throw new ParserException("Unrecognized measure type %s", measureType);
                    }
                    return true;
                }
        }
        throw new ParserException("Unrecognized measure %s", measure);
    }


    private int matchBlankLines() {
        int start = i;
        while (input[i].isBlank()) {
            i++;
        }
        return i - start;
    }
}
