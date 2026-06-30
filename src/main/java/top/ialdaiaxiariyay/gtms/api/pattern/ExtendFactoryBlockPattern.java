package top.ialdaiaxiariyay.gtms.api.pattern;

import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;

import net.minecraft.resources.ResourceLocation;

import com.google.common.base.Joiner;
import it.unimi.dsi.fastutil.chars.Char2ObjectArrayMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.CharArrayList;
import it.unimi.dsi.fastutil.chars.CharList;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ExtendFactoryBlockPattern {

    private static final Joiner COMMA_JOIN = Joiner.on(",");
    private final List<String[]> depth;
    private final List<int[]> aisleRepetitions;
    private final Char2ObjectMap<TraceabilityPredicate> symbolMap;
    private final RelativeDirection[] structureDir;
    private int aisleHeight;
    private int rowWidth;

    /**
     * Constructs a new pattern builder with specified axis directions.
     *
     * @param charDir   direction along which characters in a row increase
     * @param stringDir direction along which strings in an aisle increase
     * @param aisleDir  direction along which aisles increase
     */
    public ExtendFactoryBlockPattern(RelativeDirection charDir, RelativeDirection stringDir,
                                     RelativeDirection aisleDir) {
        depth = new ArrayList<>();
        aisleRepetitions = new ArrayList<>();
        symbolMap = new Char2ObjectArrayMap<>();
        structureDir = new RelativeDirection[3];
        structureDir[0] = charDir;
        structureDir[1] = stringDir;
        structureDir[2] = aisleDir;
        int flags = 0;
        for (int i = 0; i < 3; i++) {
            switch (structureDir[i]) {
                case UP, DOWN -> flags |= 0x1;
                case LEFT, RIGHT -> flags |= 0x2;
                case FRONT, BACK -> flags |= 0x4;
            }
        }
        if (flags != 0x7) throw new IllegalArgumentException("Must have 3 different axes!");
        this.symbolMap.put(' ', Predicates.any());
    }

    public ExtendFactoryBlockPattern aisleRepeatable(int minRepeat, int maxRepeat, String... aisle) {
        if (!ArrayUtils.isEmpty(aisle) && !StringUtils.isEmpty(aisle[0])) {
            if (this.depth.isEmpty()) {
                this.aisleHeight = aisle.length;
                this.rowWidth = aisle[0].length();
            }

            if (aisle.length != this.aisleHeight) {
                throw new IllegalArgumentException("Expected aisle with height of " + this.aisleHeight +
                        ", but was given one with a height of " + aisle.length + ")");
            } else {
                for (String s : aisle) {
                    if (s.length() != this.rowWidth) {
                        throw new IllegalArgumentException(
                                "Not all rows in the given aisle are the correct width (expected " + this.rowWidth +
                                        ", found one with " + s.length() + ")");
                    }

                    for (char c0 : s.toCharArray()) {
                        if (!this.symbolMap.containsKey(c0)) {
                            this.symbolMap.put(c0, null);
                        }
                    }
                }

                this.depth.add(aisle);
                if (minRepeat > maxRepeat)
                    throw new IllegalArgumentException("Lower bound of repeat counting must smaller than upper bound!");
                aisleRepetitions.add(new int[] { minRepeat, maxRepeat });
                return this;
            }
        } else {
            throw new IllegalArgumentException("Empty pattern for aisle");
        }
    }

    public ExtendFactoryBlockPattern aisle(String... aisle) {
        return aisleRepeatable(1, 1, aisle);
    }

    public ExtendFactoryBlockPattern setRepeatable(int minRepeat, int maxRepeat) {
        if (minRepeat > maxRepeat)
            throw new IllegalArgumentException("Lower bound of repeat counting must smaller than upper bound!");
        aisleRepetitions.set(aisleRepetitions.size() - 1, new int[] { minRepeat, maxRepeat });
        return this;
    }

    public ExtendFactoryBlockPattern setRepeatable(int repeatCount) {
        return setRepeatable(repeatCount, repeatCount);
    }

    @Contract(" -> new")
    public static @NotNull ExtendFactoryBlockPattern start() {
        return new ExtendFactoryBlockPattern(RelativeDirection.LEFT, RelativeDirection.UP, RelativeDirection.FRONT);
    }

    @Contract("_, _, _ -> new")
    public static @NotNull ExtendFactoryBlockPattern start(RelativeDirection charDir, RelativeDirection stringDir,
                                                           RelativeDirection aisleDir) {
        return new ExtendFactoryBlockPattern(charDir, stringDir, aisleDir);
    }

    public ExtendFactoryBlockPattern where(@NotNull String symbol, TraceabilityPredicate blockMatcher) {
        return this.where(symbol.charAt(0), blockMatcher);
    }

    public ExtendFactoryBlockPattern where(char symbol, @NotNull TraceabilityPredicate blockMatcher) {
        if (blockMatcher.isAny() || blockMatcher.isAir()) {
            this.symbolMap.put(symbol, blockMatcher);
        } else {
            this.symbolMap.put(symbol, new TraceabilityPredicate(blockMatcher).sort());
        }
        return this;
    }

    public BlockPattern build() {
        this.checkMissingPredicates();
        int[] centerOffset = new int[5];
        int[][] aisleRepetitions = this.aisleRepetitions.toArray(new int[this.aisleRepetitions.size()][]);
        TraceabilityPredicate[][][] predicate = (TraceabilityPredicate[][][]) Array
                .newInstance(TraceabilityPredicate.class, this.depth.size(), this.aisleHeight, this.rowWidth);

        for (int i = 0, minZ = 0, maxZ = 0; i <
                this.depth.size(); minZ += aisleRepetitions[i][0], maxZ += aisleRepetitions[i][1], i++) {
            for (int j = 0; j < this.aisleHeight; j++) {
                for (int k = 0; k < this.rowWidth; k++) {
                    predicate[i][j][k] = this.symbolMap.get(this.depth.get(i)[j].charAt(k));
                    if (predicate[i][j][k].isController) {
                        centerOffset = new int[] { k, j, i, minZ, maxZ };
                    }
                }
            }
        }

        return new BlockPattern(predicate, structureDir, aisleRepetitions, centerOffset);
    }

    public RelativeDirection getCharDir() {
        return structureDir[0];
    }

    public RelativeDirection getStringDir() {
        return structureDir[1];
    }

    public RelativeDirection getAisleDir() {
        return structureDir[2];
    }

    /**
     * Loads a pattern definition from a resource file inside the mod JAR.
     * File format: each line is a function call like:
     * direction(LEFT, UP, FRONT) (optional, must appear before any aisle lines)
     * aisle("string1", "string2", ...)
     * aisleRepeatable(2, 4, "string1", "string2", ...)
     * aisleRepeatable(3, "string1", "string2", ...)
     * Lines starting with '#' are comments; empty lines are ignored.
     * All strings must preserve leading/trailing spaces.
     * <p>
     * This method does NOT cache the result. Use {@link #fromResourceCached(ResourceLocation, Charset)} for repeated
     * usage with large patterns.
     *
     * @param location resource location, e.g. new ResourceLocation("gtms", "multiblock/spun_time_anchor.mb")
     * @param charset  character set, typically StandardCharsets.UTF_8
     * @return a configured ExtendFactoryBlockPattern
     * @throws IOException if the resource cannot be read or parsing fails
     */
    public static @NotNull ExtendFactoryBlockPattern fromResource(@NotNull ResourceLocation location,
                                                                  Charset charset) throws IOException {
        String resourcePath = "/assets/" + location.getNamespace() + "/" + location.getPath();
        try (var is = ExtendFactoryBlockPattern.class.getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new IOException("Resource not found: " + location + " (path: " + resourcePath + ")");
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, charset))) {
                return parseFromReader(reader, location.toString());
            }
        }
    }

    public static @NotNull ExtendFactoryBlockPattern fromResourceCached(ResourceLocation location) {
        return fromResourceCached(location, StandardCharsets.UTF_8);
    }

    /**
     * Loads (or retrieves from cache) an UNBOUND pattern builder from a resource file.
     * The resulting builder contains all aisle definitions and symbols (with null predicates),
     * ready for {@link #where(char, TraceabilityPredicate)} calls.
     * <p>
     * The file is parsed only once; subsequent calls return a new instance built from cached data.
     * This is highly efficient for large pattern files (>100KB).
     * <p>
     * The direction specified in the file (or default) is preserved.
     *
     * @param location resource location
     * @param charset  character set
     * @return a new ExtendFactoryBlockPattern instance (unbound, all symbols null)
     * @throws UncheckedIOException if loading fails
     */
    public static @NotNull ExtendFactoryBlockPattern fromResourceCached(ResourceLocation location, Charset charset) {
        ParsedPattern parsed = PARSED_CACHE.computeIfAbsent(location, loc -> {
            try {
                return parseAndCache(loc, charset);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
        ExtendFactoryBlockPattern fresh = new ExtendFactoryBlockPattern(
                parsed.charDir(), parsed.stringDir(), parsed.aisleDir());
        for (String[] aisle : parsed.depth) {
            fresh.depth.add(aisle.clone());
        }
        for (int[] rep : parsed.aisleRepetitions) {
            fresh.aisleRepetitions.add(rep.clone());
        }
        fresh.aisleHeight = parsed.aisleHeight;
        fresh.rowWidth = parsed.rowWidth;
        for (char c : parsed.symbols) {
            if (c != ' ') {
                fresh.symbolMap.put(c, null);
            }
        }
        return fresh;
    }

    /**
     * Cached data: includes direction because it's part of the file content.
     */
    private record ParsedPattern(List<String[]> depth,
                                 List<int[]> aisleRepetitions,
                                 int aisleHeight,
                                 int rowWidth,
                                 char[] symbols,
                                 RelativeDirection charDir,
                                 RelativeDirection stringDir,
                                 RelativeDirection aisleDir) {}

    private static final Map<ResourceLocation, ParsedPattern> PARSED_CACHE = new ConcurrentHashMap<>();

    @Contract("_, _ -> new")
    private static @NotNull ParsedPattern parseAndCache(ResourceLocation location, Charset charset) throws IOException {
        ExtendFactoryBlockPattern temp = fromResource(location, charset);
        CharList symbolList = new CharArrayList();
        for (char c : temp.symbolMap.keySet()) {
            if (c != ' ') {
                symbolList.add(c);
            }
        }
        char[] symbols = symbolList.toCharArray();
        return new ParsedPattern(
                new ArrayList<>(temp.depth),
                new ArrayList<>(temp.aisleRepetitions),
                temp.aisleHeight,
                temp.rowWidth,
                symbols,
                temp.getCharDir(),
                temp.getStringDir(),
                temp.getAisleDir());
    }

    /**
     * Parses the reader content. Supports an optional 'direction(...)' directive.
     * The directive must appear before any aisle definition and at most once.
     */
    private static @NotNull ExtendFactoryBlockPattern parseFromReader(@NotNull BufferedReader reader,
                                                                      String sourceName) throws IOException {
        List<String> lines = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null) {
            lines.add(line);
        }

        RelativeDirection charDir = RelativeDirection.LEFT;
        RelativeDirection stringDir = RelativeDirection.UP;
        RelativeDirection aisleDir = RelativeDirection.FRONT;
        boolean directionFound = false;

        List<String> remainingLines = new ArrayList<>();
        for (String rawLine : lines) {
            String trimmed = rawLine.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                continue;
            }
            if (trimmed.startsWith("direction(") && trimmed.endsWith(")")) {
                if (directionFound) {
                    throw new IOException(sourceName + ": duplicate direction directive");
                }
                String argsPart = trimmed.substring("direction(".length(), trimmed.length() - 1).trim();
                String[] parts = argsPart.split(",");
                if (parts.length != 3) {
                    throw new IOException(sourceName + ": direction requires exactly 3 arguments");
                }
                try {
                    charDir = RelativeDirection.valueOf(parts[0].trim().toUpperCase());
                    stringDir = RelativeDirection.valueOf(parts[1].trim().toUpperCase());
                    aisleDir = RelativeDirection.valueOf(parts[2].trim().toUpperCase());
                } catch (IllegalArgumentException e) {
                    throw new IOException(sourceName + ": invalid direction name", e);
                }
                directionFound = true;
            } else {
                remainingLines.add(rawLine);
            }
        }

        ExtendFactoryBlockPattern pattern = new ExtendFactoryBlockPattern(charDir, stringDir, aisleDir);

        int lineNum = 0;
        for (String rawLine : remainingLines) {
            lineNum++;
            String trimmed = rawLine.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) continue;

            int parenIndex = trimmed.indexOf('(');
            if (parenIndex < 0) throw new IOException(sourceName + " line " + lineNum + ": missing '('");
            String funcName = trimmed.substring(0, parenIndex).trim();
            String argsPart = trimmed.substring(parenIndex + 1);
            if (!argsPart.endsWith(")")) throw new IOException(sourceName + " line " + lineNum + ": missing ')'");
            argsPart = argsPart.substring(0, argsPart.length() - 1).trim();

            List<String> args = parseArguments(argsPart, sourceName, lineNum);

            if (funcName.equals("aisle")) {
                if (args.isEmpty()) throw new IOException(
                        sourceName + " line " + lineNum + ": aisle needs at least one string argument");
                pattern.aisle(args.toArray(new String[0]));
            } else if (funcName.equals("aisleRepeatable")) {
                if (args.size() < 2) throw new IOException(
                        sourceName + " line " + lineNum + ": aisleRepeatable needs at least 2 arguments");
                int min, max;
                String firstArg = args.get(0);
                if (firstArg.matches("\\d+")) {
                    int cnt = Integer.parseInt(firstArg);
                    min = max = cnt;
                    args = args.subList(1, args.size());
                } else if (firstArg.matches("\\d+,\\d+")) {
                    String[] parts = firstArg.split(",");
                    min = Integer.parseInt(parts[0]);
                    max = Integer.parseInt(parts[1]);
                    args = args.subList(1, args.size());
                } else {
                    throw new IOException(sourceName + " line " + lineNum + ": invalid repeat specifier: " + firstArg);
                }
                if (args.isEmpty()) throw new IOException(sourceName + " line " + lineNum +
                        ": aisleRepeatable needs at least one string argument after repeat specifier");
                pattern.aisleRepeatable(min, max, args.toArray(new String[0]));
            } else {
                throw new IOException(sourceName + " line " + lineNum + ": unknown function '" + funcName + "'");
            }
        }
        return pattern;
    }

    private static @NotNull List<String> parseArguments(@NotNull String argsPart, String sourceName,
                                                        int lineNum) throws IOException {
        List<String> result = new ArrayList<>();
        int i = 0;
        int len = argsPart.length();
        while (i < len) {
            while (i < len && argsPart.charAt(i) == ' ') i++;
            if (i >= len) break;

            if (argsPart.charAt(i) == '"') {
                i++;
                StringBuilder sb = new StringBuilder();
                while (i < len && argsPart.charAt(i) != '"') {
                    if (argsPart.charAt(i) == '\\' && i + 1 < len) {
                        char next = argsPart.charAt(i + 1);
                        if (next == '"' || next == '\\') {
                            sb.append(next);
                            i += 2;
                            continue;
                        }
                    }
                    sb.append(argsPart.charAt(i));
                    i++;
                }
                if (i >= len) throw new IOException(sourceName + " line " + lineNum + ": unclosed string");
                i++;
                result.add(sb.toString());
            } else {
                int start = i;
                while (i < len && argsPart.charAt(i) != ',' && argsPart.charAt(i) != ' ') i++;
                String token = argsPart.substring(start, i).trim();
                result.add(token);
            }
            while (i < len && argsPart.charAt(i) == ' ') i++;
            if (i < len && argsPart.charAt(i) == ',') i++;
        }
        return result;
    }

    private void checkMissingPredicates() {
        CharList list = new CharArrayList();
        for (var entry : this.symbolMap.char2ObjectEntrySet()) {
            if (entry.getValue() == null) {
                list.add(entry.getCharKey());
            }
        }
        if (!list.isEmpty()) {
            throw new IllegalStateException("Predicates for character(s) " + COMMA_JOIN.join(list) + " are missing");
        }
    }
}
