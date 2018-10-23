var gulp = require('gulp');
var sass = require('gulp-sass');
var autoprefixer = require('gulp-autoprefixer');
var uglifycss = require('gulp-uglifycss');
var uglifyjs = require('gulp-uglify');

gulp.task('default', ['watch']);

gulp.task('watch', function() {
    gulp.watch('app/scripts/*.js', ['compress-js']);
    gulp.watch('app/styles/*.scss', ['work-on-css']);
});

gulp.task('compress-js', function() {
    gulp.src('app/scripts/*.js')
    .pipe(uglifyjs())
    .pipe(gulp.dest('app/dist/js'))
});

gulp.task('work-on-css', function() {
    gulp.src('app/styles/*.scss')
    .pipe(sass())
    .pipe(autoprefixer({
        browsers: ['last 2 versions'],
        cascade: false
    }))
    .pipe(uglifycss({
        maxLineLen: 80,
        uglyComments: true
    }))
    .pipe(gulp.dest('app/dist/css'))
})