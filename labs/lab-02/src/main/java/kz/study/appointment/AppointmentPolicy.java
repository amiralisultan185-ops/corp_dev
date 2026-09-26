package kz.study.appointment;

public class AppointmentPolicy {
    public AppointmentStatus move(AppointmentStatus from, AppointmentStatus to){
        boolean fromRequested = from instanceof AppointmentStatus.Requested;
        boolean toConfirmed = to instanceof AppointmentStatus.Confirmed;
        if (fromRequested && toConfirmed){
            return to;
        }
        boolean fromConfirmed = from instanceof AppointmentStatus.Confirmed;
        boolean toCompleted = to instanceof AppointmentStatus.Completed;
        if (fromConfirmed && toCompleted){
            return to;
        }else {
            throw new IllegalStateException("Transition is not allowed");
        }
    }
}
