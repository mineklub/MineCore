package dk.mineclub.minecore.common.validator;

import dk.mineclub.minecore.common.dto.MinecoreRequestProductDto;
import java.lang.annotation.*;
import java.util.List;
import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;

/** Constraint validator that ensures at most one product has subscriptionDays set. */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AtMostOneSubscriptionValidator.class)
@Documented
public @interface AtMostOneSubscription {

    String message() default "Only one product may have subscriptionDays set";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

class AtMostOneSubscriptionValidator
        implements ConstraintValidator<AtMostOneSubscription, List<MinecoreRequestProductDto>> {

    @Override
    public boolean isValid(
            List<MinecoreRequestProductDto> products, ConstraintValidatorContext context) {
        if (products == null) {
            return true;
        }

        long count = products.stream().filter(p -> p.getSubscriptionDays() != null).count();

        return count <= 1;
    }
}
