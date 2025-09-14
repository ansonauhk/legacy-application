package com.example.legacyapp.util;

import com.example.legacyapp.model.User;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import org.springframework.stereotype.Component;
import sun.misc.BASE64Encoder;
import sun.misc.BASE64Decoder;
import javax.annotation.Nullable;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class Java8Features {

    private final BASE64Encoder encoder = new BASE64Encoder();
    private final BASE64Decoder decoder = new BASE64Decoder();

    public void processUserWithLambda(User user) {
        List<String> attributes = Arrays.asList(user.getUsername(), user.getEmail());
        
        List<String> processedAttributes = attributes.stream()
                .filter(Objects::nonNull)
                .map(String::toUpperCase)
                .collect(Collectors.toList());
        
        processedAttributes.forEach(System.out::println);
        
        Optional<String> firstAttribute = attributes.stream()
                .filter(attr -> attr != null && attr.length() > 5)
                .findFirst();
        
        firstAttribute.ifPresent(attr -> {
            System.out.println("First valid attribute: " + attr);
        });
    }

    public String encodeString(String input) {
        return encoder.encode(input.getBytes());
    }

    public String decodeString(String encoded) throws IOException {
        return new String(decoder.decodeBuffer(encoded));
    }

    public List<String> processWithGuava(List<User> users) {
        Predicate<User> activeUserPredicate = new Predicate<User>() {
            @Override
            public boolean apply(@Nullable User user) {
                return user != null && user.getUsername() != null;
            }
        };
        
        Function<User, String> userToStringFunction = new Function<User, String>() {
            @Override
            public String apply(@Nullable User user) {
                return user != null ? user.getUsername() : "";
            }
        };
        
        return FluentIterable.from(users)
                .filter(activeUserPredicate)
                .transform(userToStringFunction)
                .toList();
    }

    public void reflectionExample(Object obj) {
        Class<?> clazz = obj.getClass();
        Field[] fields = clazz.getDeclaredFields();
        
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object value = field.get(obj);
                System.out.println(field.getName() + " = " + value);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public void useInternalAPI() {
        sun.misc.Unsafe unsafe = null;
        try {
            Field f = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            unsafe = (sun.misc.Unsafe) f.get(null);
            System.out.println("Unsafe instance obtained: " + (unsafe != null));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        System.out.println("Java8Features object is being garbage collected");
        super.finalize();
    }

    public Map<String, List<User>> groupUsersByEmail(List<User> users) {
        return users.stream()
                .filter(user -> user.getEmail() != null)
                .collect(Collectors.groupingBy(
                    User::getEmail,
                    LinkedHashMap::new,
                    Collectors.toList()
                ));
    }

    public void demonstrateOptional() {
        Optional<String> optional = Optional.of("value");
        
        optional.filter(s -> s.length() > 3)
                .map(String::toUpperCase)
                .ifPresent(System.out::println);
        
        String result = optional.orElse("default");
        String computed = optional.orElseGet(() -> "computed default");
        
        try {
            String required = optional.orElseThrow(() -> new IllegalStateException("No value present"));
        } catch (IllegalStateException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}