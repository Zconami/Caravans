package com.zconami.Caravans.domain;

public interface EntityObserver<E> {

    void entityChanged(E entity);

}
