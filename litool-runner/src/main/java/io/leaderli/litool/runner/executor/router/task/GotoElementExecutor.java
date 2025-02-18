package io.leaderli.litool.runner.executor.router.task;

import io.leaderli.litool.runner.Context;
import io.leaderli.litool.runner.Interrupt;
import io.leaderli.litool.runner.executor.BaseElementExecutor;
import io.leaderli.litool.runner.xml.router.task.GotoElement;

public class GotoElementExecutor extends BaseElementExecutor<GotoElement> {
    public GotoElementExecutor(GotoElement element) {
        super(element);
    }

    @Override
    public void execute(Context context) {
        context.interrupt.set(Interrupt.GOTO);
        context.interruptObj = element.getNext().next;
    }
}
