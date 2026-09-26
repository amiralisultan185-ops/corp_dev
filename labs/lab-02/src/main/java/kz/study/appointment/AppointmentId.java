package kz.study.appointment;

public record AppointmentId(String value) {
    public AppointmentId {
        if(value ==null || value.isBlank()){
            throw new IllegalArgumentException(
                    "This place should not stand empty."
            );
        }
    }
}
