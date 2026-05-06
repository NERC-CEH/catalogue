package uk.ac.ceh.gateway.catalogue.waf;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import static uk.ac.ceh.gateway.catalogue.CatalogueMediaTypes.GEMINI_XML_SHORT;

/**
 * The following emulates a Web accessible Folder of gemini metadata records
 * from the current catalogue
 */
@Slf4j
@ToString
@Controller
@RequestMapping("documents/gemini/waf")
public class GeminiWafController {
    private final GeminiWafService geminiWafService;

    public GeminiWafController( GeminiWafService geminiWafService ) {
        this.geminiWafService = geminiWafService;
        log.info("Creating");
    }

    @GetMapping("/")
    public ModelAndView getWaf() {
        return new ModelAndView("/html/waf", "files", geminiWafService.getWafFiles());
    }

    @GetMapping("{id}.xml")
    public String forwardToMetadata(@PathVariable("id") String id) {
        return "forward:/documents/" + id + "?format=" + GEMINI_XML_SHORT;
    }
}
