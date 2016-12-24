package lab.io.rush.Controller;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lab.io.rush.Entity.User;
import lab.io.rush.Service.UserService;
import lab.io.rush.Util.Mymail;

import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import redis.clients.jedis.Jedis;

@Component
@RequestMapping("/buy.do")
public class buyController  {

	@Resource
	private UserService userService;
	
	@RequestMapping
	public ModelAndView handleRequest(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		System.out.println("buy.do");
		Jedis jedis = new Jedis("127.0.0.1", 6379);
		String filmid = request.getParameter("id");
		String filmname = request.getParameter("filmname");
		int number = Integer.parseInt(jedis.get(filmid));
		String username = (String)request.getSession().getAttribute("user");

		if(number > 0){
			String record = jedis.get(filmid+"record");
			if(record == null || record.isEmpty() || !(record.contains(username))){
				record = record +";" +username;
				jedis.set(filmid, (number-1)+"");
				jedis.set(filmid+"record", record);
				 final User user = userService.findUserByname(username);
				 final String content = "亲爱的" + user.getName()+"恭喜你，成功购买电影票《"+filmname+"》，请准时观看！";
				new Thread(){
					public void run() {
						Mymail mail = new Mymail("电影票抢购成功",content,user.getMail());
						try {
							mail.send();
						} catch (MessagingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}	
					};
				}.start();
				response.getWriter().write("购买成功");
			}else{
				response.getWriter().write("您已经购买了电影票，不能重新购买");
			}
		}else{
			response.getWriter().write("不好意思，电影票已经售空！");
		}
		
		jedis.close();
		return null;
	}

}