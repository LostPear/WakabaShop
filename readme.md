# Wakaba Shop
一个仿照《跟 Wakaba 酱一起学 Web 开发》里的示例网页开发的项目。

## 本地开发环境准备
1. clone 本项目，命令行定位到 web 文件夹下，输入以下命令安装依赖包：
```bash
npm install
```
2. 运行以下命令启动 Tailwind CSS：
```bash
npx @tailwindcss/cli -i ./src/css/input.css -o ./src/css/output.css --watch
```
3. 在 IDEA 中打开该项目，使用 Maven 同步 pom.xml 中的依赖包，然后在项目结构中添加对应的 Web 配置。
4. 在 IDEA 中配置 tomcat，使用 tomcat 运行项目。


** 本项目使用了 Meraki UI 实现部分网页效果，部分 JavaScript 实现使用了 Alpine.js ，由衷感谢各位的付出。**