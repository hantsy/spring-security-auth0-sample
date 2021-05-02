import { Injectable } from '@angular/core';
import {
  Resolve,
  ActivatedRouteSnapshot,
  RouterStateSnapshot,
} from '@angular/router';
import { PostService } from './post.service';
import { Post } from './post.model';

@Injectable()
export class PostDetailsResolve implements Resolve<Post> {
  constructor(private postService: PostService) {}

  resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {
    const postId = route.paramMap.get('id');

    if (postId) {
      return this.postService.getPost(postId);
    }

    throw new Error('post id is required.');
  }
}
