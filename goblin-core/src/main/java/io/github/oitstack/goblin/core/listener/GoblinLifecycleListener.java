package io.github.oitstack.goblin.core.listener;

import io.github.oitstack.goblin.core.Goblin;

/**
 * @Author CuttleFish 80317381
 * @Date 2022/7/22 下午2:47
 */
public interface GoblinLifecycleListener {
    public void onStarted(Goblin goblin);
    public void onDestroyed(Goblin goblin);
}
