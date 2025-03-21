### Описание идеи

Java-Agent при запуске приложения начинает сканировать все загружаемые классы.
Если находит файл с хотя бы одним synchronized методом или блоком, модифицирует его, используя ASM, иначе класс
загружается в неизменном виде.

#### Модификация методов следующая:

1) С `synchronized` метода снимается флаг `ACC_SYNCHRONIZED` (перестаёт быть `synchronized`), всё его тело
   подразумевается как будто находится в блоке `synchronized (this)`.
2) В начале метода создаётся новая переменная, ссылающаяся на новую `ArrayDeque<Lock>`.
3) Блоки `synchronized` заменяются на вызов внешнего статического метода `SharedReentrantLock::lock`, возвращающего
   `Lock` (с аргументом = объектом, указанным в блоке). Лок возвращается уже взятым!
4) Результат вызова складывается в `ArrayDeque<Lock>`.
5) Сам блок удаляется (если он был), заменяется на try-finally, где в блоке finally производится `ArrayDeque::pop` и
   вызов `unlock` на вернувшемся объекте.
6) `SharedReentrantLock::lock` при взятии помещается в статичную `ConcurrentHashMap<Object, ReentrantLock>`, чтобы
   синхронизация на одном и том же объекте возвращала один и тот же лок.
7) Во время вызова `unlock` перед фактическим освобождением лок пытается удалить себя из `Map<Object, ReentrantLock>`,
   если после освобождения `holdCount` будет равен нулю.
