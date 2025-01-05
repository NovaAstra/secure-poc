package com.nebula.gateway.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

@Component
public class PathUtils {
  private final List<MatcherRule> matcherRules = new ArrayList<>();

  public PathUtils(String[] excludedPaths) {
    for (String path : excludedPaths) {
      if (isComplexPattern(path)) {
        String regex = convertToRegex(path);
        matcherRules.add(new PatternMatcherRule(Pattern.compile(regex)));
      } else {
        matcherRules.add(new PrefixMatcherRule(path));
      }
    }
  }

  /**
   * 检查路径是否匹配排除规则
   *
   * @param path 待检查的路径
   * @return 如果匹配排除规则，返回 true；否则返回 false
   */
  public boolean matches(String path) {
    for (MatcherRule rule : matcherRules) {
      if (rule.matches(path)) {
        return true;
      }
    }
    return false;
  }

  /**
   * 判断是否是复杂模式（包含通配符或正则表达式）
   *
   * @param path 路径
   * @return 是否是复杂模式
   */
  private boolean isComplexPattern(String path) {
    return path.contains("*") || path.contains("^") || path.contains("$") || path.contains(".");
  }

  /**
   * 将路径转换为正则表达式
   *
   * @param path 路径
   * @return 对应的正则表达式
   */
  private String convertToRegex(String path) {
    String regex = "^" + path
        .replace(".", "\\.")
        .replace("*", ".*")
        + ".*";
    return regex;
  }

  /**
   * 简单前缀匹配规则
   */
  private static class PrefixMatcherRule implements MatcherRule {
    private final String prefix;

    PrefixMatcherRule(String prefix) {
      this.prefix = prefix;
    }

    @Override
    public boolean matches(String path) {
      return path.startsWith(prefix);
    }
  }

  /**
   * 复杂模式匹配规则
   */
  private static class PatternMatcherRule implements MatcherRule {
    private final Pattern pattern;

    PatternMatcherRule(Pattern pattern) {
      this.pattern = pattern;
    }

    @Override
    public boolean matches(String path) {
      return pattern.matcher(path).matches();
    }
  }

  private interface MatcherRule {
    boolean matches(String path);
  }
}
