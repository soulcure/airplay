### 初始化、更新
    git submodule update --init --recursive
    git submodule update --recursive

### 目录结构
![avatar](doc/top.png)

    按照架构图，分为两类。
    1、api：模块对外提供的api能力封装，包含API层
    2、core：模块的实现，包含Runtime和Service Component层

* 添加api模块

    ```
    api模块是独立仓库：
        以git submoudle方式添加到api目录下，并在api目录下添加settings_xxx.gradle文件，里面配置对应api模块的依赖

        例：添加UserApi
        1、在api目录下执行 git submodule add xxxxxxxxxx.git user
        2、在api目录下添加 settings_user.gradle，关联上user内部的模块
        3、user-api的project名为:user-api，在api目录的 build.gradle 中添加 api project(':user-api')
    ```

    ```
    api模块和core在同一仓库：
        按照添加core模块的方式，以git submodule的方式将core模块添加到core目录中。在api目录下添加settings_xxx.gradle文件，里面配置对应api模块的依赖

        例：添加UserApi
        1、在core添加user core模块
        2、在api目录下添加 settings_user.gradle，关联上core目录中user-api的内部模块
        3、user-api的project名为:user-api，在api目录的 build.gradle 中添加 api project(':user-api')
    ```


* 添加core模块

    ```
    以git submoudle方式添加到core目录下，并在api目录下添加settings_xxx.gradle文件，里面配置对应core模块的依赖

    例：添加UserService
    1、在core目录下执行 git submodule add xxxxxxxxxx.git user
    2、在core目录下添加 settings_user.gradle，关联上user内部的模块
    3、user-service的project名为:user-service，在core目录的 build.gradle 中添加 api project(':user-service')
    ```