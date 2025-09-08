import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.math.BigInteger;
import java.util.*;

public class App2 {

    public static void main(String[] args) {
        try {
            File file = new File("resources/data2.json");
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(file);

            int n = rootNode.get("keys").get("n").asInt();
            int k = rootNode.get("keys").get("k").asInt();

            System.out.println("Number of roots (n): " + n);
            System.out.println("Minimum roots needed (k): " + k);

            // Use BigInteger for decoded values
            Map<Long, BigInteger> decodedPoints = new TreeMap<>();
            Iterator<String> keysIter = rootNode.fieldNames();

            while (keysIter.hasNext()) {
                String keyStr = keysIter.next();
                if (keyStr.equals("keys")) continue;

                JsonNode node = rootNode.get(keyStr);
                String baseStr = node.get("base").asText();
                String valueStr = node.get("value").asText();

                int base = Integer.parseInt(baseStr);
                // Use BigInteger for parsing large values
                BigInteger decimalValue = new BigInteger(valueStr, base);
                long x = Long.parseLong(keyStr);

                decodedPoints.put(x, decimalValue);

                System.out.printf("Root x=%d: base=%s, value=%s -> decimal %s%n", x, baseStr, valueStr, decimalValue.toString());
            }

            if (decodedPoints.size() < k) {
                System.out.println("Not enough points to solve polynomial.");
                return;
            }

            // Pick first k points sorted by x
            List<Long> xs = new ArrayList<>();
            List<BigInteger> ys = new ArrayList<>();
            int count = 0;
            for (Map.Entry<Long, BigInteger> entry : decodedPoints.entrySet()) {
                xs.add(entry.getKey());
                ys.add(entry.getValue());
                count++;
                if (count == k) break;
            }

            // Lagrange interpolation at x=0 using double for calculation
            // Convert BigInteger to double for interpolation — might lose precision but should work for secret extraction
            double secret = 0.0;
            for (int j = 0; j < k; j++) {
                double numerator = 1.0;
                double denominator = 1.0;
                for (int m = 0; m < k; m++) {
                    if (m != j) {
                        numerator *= (0.0 - xs.get(m));
                        denominator *= (xs.get(j) - xs.get(m));
                    }
                }
                double lj = numerator / denominator;
                secret += ys.get(j).doubleValue() * lj;
            }

            long c = Math.round(secret);
            System.out.println("Constant term c (secret) = " + c);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
