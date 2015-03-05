package models

/**
 * Created by alper on 28/11/14.
 */
case class AttendanceLog(_id:Long,id_attendance_event:Long,id_employee:Long,login_combination:Int,
                          date:Long,status:Int,annotation:String,processed:Int,id_device:String)

object AttendanceLog{

  /**
   * This variable necessary for all core_operation_log data saving
   */
  val table = "table=attendance_log;"

  def fromParser(
                _id:Long = 0,
                id_attendance_event:Long = 0,
                id_employee:Long =0,
                login_combination:Int = 0,
                date:Long = 0,
                status:Int = 0,
                annotation:String = "",
                processed:Int = 0,
                id_device:String = ""
                  ):AttendanceLog = {
    new AttendanceLog(
      _id,
      id_attendance_event,
      id_employee,
      login_combination,
      date,
      status,
      annotation,
      processed,
      id_device
    )
  }

}
