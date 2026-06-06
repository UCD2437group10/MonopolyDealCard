package edu.group10.common.skill;

import edu.group10.common.model.Command;
import edu.group10.common.model.SkillContext;
import java.util.List;

public interface SkillResolver {
    List<Command> resolve(SkillContext context);
}
