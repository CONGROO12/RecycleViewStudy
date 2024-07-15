# android开发RecyclerView学习实践

## 多种布局格式的RecyclerView

涉及知识点：Activity、RecyclerView、自定义LayoutManager、SharedPreference、Service

![多种布局格式的RecyclerView](https://github.com/user-attachments/assets/0fcdf5a5-aca6-4aa6-b6f6-1555fc25618d)

  - ### 垂直滑动布局
    
    ![垂直滑动布局](https://github.com/user-attachments/assets/372f2fd8-9194-40ab-83ba-01fe6ef49583)


  - ### 水平滑动布局
    
    ![水平滑动布局](https://github.com/user-attachments/assets/d269faec-5fd9-4a04-bd2c-2324fabd84f5)


  - ### 网格滑动布局
    
    ![网格滑动布局](https://github.com/user-attachments/assets/cdacad44-b0db-4e4c-9d42-92c77344896d)


  - ### 瀑布流布局
    
    ![瀑布流布局](https://github.com/user-attachments/assets/def73c6f-7f89-441d-9d52-aee15bbaf915)


  - ### 自定义LayoutManager梯形流动布局
    
    ![自定义LayoutManager梯形流动布局](https://github.com/user-attachments/assets/8ac6883f-ee03-47b7-a4c5-d23ad815b1a1)

## 条目管理功能

点击具体条目可以编辑信息

![image](https://github.com/user-attachments/assets/cd5b81af-7d15-4e58-b0b5-8a11e43d3326)

长按可以删除条目或在本条目上方增加条目

![image](https://github.com/user-attachments/assets/dc3fcf1b-921f-434d-b59c-c7d5f7e33f8d)
![image](https://github.com/user-attachments/assets/52075c87-4d72-4b39-b374-8a6c11816e48)
![image](https://github.com/user-attachments/assets/084f89a0-776c-4f47-94f2-4f0371b44409)
![image](https://github.com/user-attachments/assets/70861fd9-133d-406c-a78c-dd8407e44a01)

## Gson序列化+SharedPreference保存列表数据

使用Gson工具将装有Item对象的myList序列化成String，作为键值对用SharedPreference保存在文件"data_item"中
> 详见SBaseActivity

## Service+MediaPlayer实现点击播放音乐《妄想税》

在MainActivity中绑定WxsService中的MusicBinder，并使用MusicBinder中的play()方法进行控制MediaPlayer的播放
> 详见WxsService和MainActivity
