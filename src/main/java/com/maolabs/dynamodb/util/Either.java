package com.maolabs.dynamodb.util;

import java.util.Optional;

/**
 * Classe para tentar evitar lançar exceções para controle de fluxo
 * @param <A>
 * @param <B>
 */
public class Either<A, B> {
    private A left = null;
    private B right = null;

    private Either(A a, B b) {
        left = a;
        right = b;
    }

    public static <A, B> Either<A, B> left(A a) {
        return new Either<A, B>(a, null);
    }

    public Optional<A> left() {
        return Optional.ofNullable(left);
    }

    public boolean isLeft() {
        return left != null;
    }

    public boolean isRight() {
        return right != null;
    }

    public B right() {
        return right;
    }

    public static <A, B> Either<A, B> right(B b) {
        return new Either<A, B>(null, b);
    }
}
