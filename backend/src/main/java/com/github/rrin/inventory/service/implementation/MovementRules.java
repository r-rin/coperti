package com.github.rrin.inventory.service.implementation;

import com.github.rrin.exception.ValidationCheck;
import com.github.rrin.exception.types.ValidationException;
import com.github.rrin.inventory.Location;
import com.github.rrin.inventory.LocationKind;
import com.github.rrin.inventory.MovementType;
import com.github.rrin.inventory.QualityState;

import java.util.EnumSet;
import java.util.Set;

import static com.github.rrin.inventory.MovementType.*;

/** What a movement of each type must look like, so the ledger stays sliceable by type alone. */
final class MovementRules {

    // types that only the owning service may post, because they carry bookkeeping beyond the ledger
    static final Set<MovementType> MANUAL = EnumSet.of(
            RECEIPT, TRANSFER, QUALITY_CHANGE, RETURN_TO_SUPPLIER, SCRAP, ISSUE_TO_EMPLOYEE, RETURN_FROM_EMPLOYEE);

    private MovementRules() {
    }

    static void requireShape(MovementType type, Location from, Location to, QualityState fromQuality, QualityState toQuality) {
        ValidationCheck check = new ValidationCheck()
                .check(!(from.getId().equals(to.getId()) && fromQuality == toQuality),
                        "A movement must change location or quality");
        switch (type) {
            case RECEIPT -> check.check(from.getKind() == LocationKind.V_SUPPLIER && !to.getKind().isVirtual(),
                    "RECEIPT moves stock from the supplier into a real location");
            case RETURN_TO_SUPPLIER -> check.check(!from.getKind().isVirtual() && to.getKind() == LocationKind.V_SUPPLIER,
                    "RETURN_TO_SUPPLIER moves stock from a real location to the supplier");
            case SCRAP -> check.check(!from.getKind().isVirtual() && to.getKind() == LocationKind.V_SCRAP,
                    "SCRAP moves stock from a real location to scrap");
            case CONSUME -> check.check(!from.getKind().isVirtual() && to.getKind() == LocationKind.V_CONSUMED,
                    "CONSUME moves stock from a real location to consumed");
            case PRODUCE, RECOVER_FROM_TEARDOWN -> check.check(
                    from.getKind() == LocationKind.V_CONSUMED && !to.getKind().isVirtual(),
                    type + " moves stock from consumed into a real location");
            case OTHER_USE -> check.check(!from.getKind().isVirtual() && to.getKind() == LocationKind.V_OTHER_USE,
                    "OTHER_USE moves stock from a real location to other use");
            case ISSUE_TO_EMPLOYEE -> check.check(!from.getKind().isVirtual() && to.getKind() == LocationKind.EMPLOYEE,
                    "ISSUE_TO_EMPLOYEE moves stock from a real location to an employee");
            case RETURN_FROM_EMPLOYEE -> check.check(from.getKind() == LocationKind.EMPLOYEE && !to.getKind().isVirtual(),
                    "RETURN_FROM_EMPLOYEE moves stock from an employee to a real location");
            case QUALITY_CHANGE -> check.check(from.getId().equals(to.getId()) && fromQuality != toQuality
                            && !from.getKind().isVirtual(),
                    "QUALITY_CHANGE changes quality in place at one real location");
            case TRANSFER -> check.check(!from.getKind().isVirtual() && !to.getKind().isVirtual()
                            && !from.getId().equals(to.getId()),
                    "TRANSFER moves stock between two different real locations");
        }
        check.throwIfAny(ValidationException::new);
    }
}
