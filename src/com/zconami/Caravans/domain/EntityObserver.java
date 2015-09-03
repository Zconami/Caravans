package com.zconami.Caravans.domain;

public interface EntityObserver<E extends Entity> {

    void entityChanged(E entity);

    void entityRemoved(E entity);

}
