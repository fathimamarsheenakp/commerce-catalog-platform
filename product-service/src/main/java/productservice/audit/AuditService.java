package productservice.audit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AuditService {

    public void log(String username, String action, String productId) {
        log.info("AUDIT | user={} | action={} | productId={}",
                username,
                action,
                productId);
    }
}