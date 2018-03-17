# YCardLayout
卡片移动动画布局
 
## 功能
实现卡片选择样式的移动、删除、复位的动画

 
## 添加依赖

Add it in your root build.gradle at the end of repositories:

```

	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```
Add the dependency
```
	dependencies {
	        compile 'com.github.lewis-v:YCardLayout:1.0.1'
	}
```
 
## 在布局中使用
```
  <com.lewis_v.ycardlayoutlib.YCardLayout
        android:id="@+id/fl"
        android:layout_marginTop="20dp"
        app:layout_constraintLeft_toLeftOf="parent"
        app:layout_constraintRight_toRightOf="parent"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content">
        <ImageView
            android:id="@+id/img"
            android:layout_margin="5dp"
            android:src="@mipmap/ic_launcher"
            android:layout_width="200dp"
            android:layout_height="200dp" />
    </com.lewis_v.ycardlayoutlib.YCardLayout>
```
## 代码中进行操作
控件中已有默认的配合参数,所以可以直接使用,不进行配置
```
yCardLayout = findViewById(R.id.fl);
        //yCardLayout.setMaxWidth(yCardLayout.getWidth());//设置最大移动距离
        //yCardLayout.setMoveRotation(45);//最大旋转角度
        //yCardLayout.reset();//重置数据

        img = findViewById(R.id.img);
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                yCardLayout.removeToLeft(null);
                Toast.makeText(MainActivity.this,"点击11",Toast.LENGTH_SHORT).show();
            }
        });
```
