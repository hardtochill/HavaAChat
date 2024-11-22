package cn.havaachat.config;

import cn.havaachat.enums.ResponseCodeEnum;
import cn.havaachat.exception.BaseException;
import cn.havaachat.utils.JacksonObjectMapper;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * WebMvc配置类
 */
@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {
    /**
     * 拓展MVC框架消息转换器，用于转换前端提交的json形式数据
     */
    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        //创建一个消息转换器对象
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        //为该对象设置消息转换器
        converter.setObjectMapper(new JacksonObjectMapper());
        // 将该消息转换器对象加入列表，同时设置为首位，优先使用
        converters.add(0, converter);
    }

    /**
     * 配置全局格式化器，用于转换前端提交的表单形式数据
     * @param registry
     */
    @Override
    public void addFormatters(FormatterRegistry registry) {
        // 将"yyyy-MM-dd"形式字符串转LocalDate
        registry.addConverter(new Converter<String, LocalDate>() {
            private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            @Override
            public LocalDate convert(String source) {
                try {
                    return LocalDate.parse(source, dateFormatter);
                } catch (DateTimeParseException e) {
                    throw new BaseException(ResponseCodeEnum.CODE_600.getCode(),"日期格式错误，请使用'yyyy-MM-dd'形式");
                }
            }
        });

        // 将"yyyy-MM-dd HH:mm:ss"形式字符串转LocalDateTime
        registry.addConverter(new Converter<String, LocalDateTime>() {
            private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            @Override
            public LocalDateTime convert(String source) {
                try {
                    return LocalDateTime.parse(source, dateTimeFormatter);
                } catch (DateTimeParseException e) {
                    throw new BaseException(ResponseCodeEnum.CODE_600.getCode(),"日期格式错误，请使用'yyyy-MM-dd HH:mm:ss'形式");
                }
            }
        });

        // 将"HH:mm:ss"形式字符串转LocalTime
        registry.addConverter(new Converter<String, LocalTime>() {
            private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

            @Override
            public LocalTime convert(String source) {
                try {
                    return LocalTime.parse(source, timeFormatter);
                } catch (DateTimeParseException e) {
                    throw new BaseException(ResponseCodeEnum.CODE_600.getCode(),"日期格式错误，请使用'HH:mm:ss'形式");
                }
            }
        });
    }
}
