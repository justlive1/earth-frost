package justlive.earth.breeze.frost.executor.redis.model;

import java.util.Date;
import java.util.Objects;
import justlive.earth.breeze.frost.core.model.JobExecuteRecord;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 状态记录
 * 
 * @author wubo
 *
 */
@Data
@NoArgsConstructor
public class JobRecordStatus {

  private String id;

  private Integer type;

  private String status;

  private String msg;

  private Date time;

  public JobRecordStatus(JobExecuteRecord record) {
    id = record.getId();
    if (record.getExecuteStatus() == null && record.getDispachStatus() != null) {
      status = record.getDispachStatus();
      msg = record.getDispachMsg();
      time = record.getDispachTime();
      type = 0;
    } else {
      status = record.getExecuteStatus();
      msg = record.getExecuteMsg();
      time = record.getExecuteTime();
      type = 1;
    }
  }

  public void fill(JobExecuteRecord record) {
    if (Objects.equals(id, record.getId())) {
      if (type == 0) {
        record.setDispachMsg(msg);
        record.setDispachStatus(status);
        record.setDispachTime(time);
      } else {
        record.setExecuteMsg(msg);
        record.setExecuteStatus(status);
        record.setExecuteTime(time);
      }
    }
  }
}
