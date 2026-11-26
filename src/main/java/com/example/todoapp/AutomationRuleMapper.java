package com.example.todoapp;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AutomationRuleMapper {
    List<AutomationRule> findAll();
    AutomationRule findById(Long id);
    int insert(AutomationRule rule);
    int update(AutomationRule rule);
    int deleteById(Long id);
    int setEnabled(@Param("id") Long id, @Param("enabled") boolean enabled);
    List<AutomationRuleRun> findRuns(@Param("ruleId") Long ruleId);
    int insertRun(AutomationRuleRun run);
    int updateExecution(@Param("id") Long id);
    List<Todo> findActiveTodos();
    int updateTodoPriority(@Param("id") Long id, @Param("priority") Integer priority);
    int updateTodoCategory(@Param("id") Long id, @Param("category") String category);
    int updateTodoProject(@Param("id") Long id, @Param("projectId") Long projectId);
    long countUncompleted();
    long countCompletedToday();
    long sumActualMinutesToday();
}
