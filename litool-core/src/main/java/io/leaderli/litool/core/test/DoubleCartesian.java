package io.leaderli.litool.core.test;

import io.leaderli.litool.core.collection.CollectionUtils;

/**
 * @author leaderli
 * @since 2022/8/20
 */
class DoubleCartesian implements CartesianFunction<DoubleValues, Double> {
    @Override
    public Double[] apply(DoubleValues annotatedByValuable, CartesianContext context) {
        double[] value = annotatedByValuable.value();
        if (value.length > 0) {
            return CollectionUtils.toArray(value);
        }
        return CartesianUtil.cartesian_single_def(Double.class);
    }
}
