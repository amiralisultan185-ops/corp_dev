package kz.study.appointment;
public class Main {
    public static void main(String[] args) {
    AppointmentId id = new AppointmentId("apt-001");
        System.out.println(id.value());
        AppointmentPolicy policy = new AppointmentPolicy();
        AppointmentStatus from = new AppointmentStatus.Confirmed();
        AppointmentStatus to = new AppointmentStatus.Completed();
        AppointmentStatus result = policy.move(from, to);
        System.out.println(result);
    }
}